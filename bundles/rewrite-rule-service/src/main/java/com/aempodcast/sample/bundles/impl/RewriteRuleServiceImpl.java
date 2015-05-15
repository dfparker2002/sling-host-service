package com.aempodcast.sample.bundles.impl;

import java.util.*;

import org.apache.felix.scr.annotations.*;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.sling.api.resource.*;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.ComponentContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aempodcast.sample.bundles.RewriteRuleService;

import javax.jcr.*;
import javax.jcr.LoginException;
import javax.jcr.query.*;

@Component(immediate = true, metatype = true, label = "AEM Podcast Sample Service")
@Service
@Properties({
    @Property(name = "service.vendor", value = "com.aempodcast.sample"),
    @Property(name = "service.description", value = "AEM Podcast Sample OSGI service."),
})
public class RewriteRuleServiceImpl implements RewriteRuleService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Reference
    private SlingRepository repository;
    
    @Reference
    private ResourceResolverFactory resourceResolverFactory;
    
    private ResourceResolver resourceResolver;
    
    @Property(
            label = "Custom Rewrite Rules",
            description = "Additional / arbitrary rewrites to generate on output; format" +
                    " is match:target (e.g., /content/foohost/en/bookworm:/buy/a/book" +
                    " would create RewriteRule /content/foohost/en/bookworm /buy/a/book [PT, L]",
            unbounded = PropertyUnbounded.ARRAY
    )
    private static final String CUSTOM_RULES = "rewriterules.customRules";

    @Property(
            label = "Default Response Code",
            description = "Defaults to 301 (Moved Permanently)",
            intValue = 301,
            options = {
                    @PropertyOption(name = "301", value = "Moved Permanently"),
                    @PropertyOption(name = "302", value = "(Redirect) Found")
            }
    )
    private static final String DEFAULT_CODE = "rewriterules.defaultCode";
    
    @Property(
            label = "Map Custom Rewrite Rules",
            description = "If true (checked), all custom rewrite rule targets " +
                    "will be passed through resourceResolver.map() on output.",
            boolValue = true
    )
    private static final String MAP_CUSTOM_RULES = "rewriterules.mapCustomRules";
    
    private String[] customRules;
    private Boolean mapCustomRules;
    private List<String> vanityUrls = new ArrayList<String>();;
    private String[] etcMapRules;

    @Override
    public Map<String, Map<String, String>> getRules() {
        Map<String, Map<String, String>> allRules = new HashMap<String, Map<String, String>>();
        Map<String, String> customRuleMap = new HashMap<String, String>();
        Map<String, String> vanityUrls = new HashMap<String, String>();
        Map<String, String> etcMapRules = new HashMap<String, String>();

        for (String rule : this.customRules) {
            int splitPos = rule.indexOf(':');
            if (splitPos != -1) {
                customRuleMap.put(rule.substring(0, splitPos), rule.substring(splitPos + 1));
            }
        }
        allRules.put("customRules", customRuleMap);
        
        for (String rule : this.vanityUrls) {
            int splitPos = rule.indexOf(':');
            if (splitPos != -1) {
                vanityUrls.put(rule.substring(0, splitPos), rule.substring(splitPos + 1));
            }
        }
        
        allRules.put("vanityRules", vanityUrls);
        log.trace("generatedRules: {}", allRules);
        return allRules;
    }
    
    @Override
    public Boolean getMapCustomRules() {
        return this.mapCustomRules;
    }
    
    private void buildVanityUrls() throws org.apache.sling.api.resource.LoginException {
        Session session = null;
        this.resourceResolver = resourceResolverFactory.getResourceResolver(null);
        String queryString = "select * from [cq:PageContent] as content where content.[sling:vanityPath] is not null";
        try {
            session = this.repository.loginAdministrative(null);
            QueryResult queryResult = null;
            if (session != null) {
                try {
                    QueryManager queryManager = null;
                    queryManager = session.getWorkspace().getQueryManager();
                    Query query = queryManager.createQuery(queryString, Query.JCR_SQL2);
                    queryResult = query.execute();
                    RowIterator rowIterator = queryResult.getRows();
                    if (rowIterator != null) {
                        while (rowIterator.hasNext()) {
                            Row row = rowIterator.nextRow();
                            Node rowNode = row.getNode();
                            PropertyIterator propertyIterator = rowNode.getProperties();
                            while(propertyIterator != null && propertyIterator.hasNext()) {
                                javax.jcr.Property property = propertyIterator.nextProperty();
                                if (property.getName().equals("sling:vanityPath")) {
                                    String path = rowNode.getParent().getPath();
                                    if (property.isMultiple()) {
                                        for(Value url : property.getValues()) {
                                            this.vanityUrls.add(buildMatchUrl(url.getString()) + ":" + path);
                                        }
                                    } else {
                                        /* There's only one property, so don't loop */
                                        this.vanityUrls.add(buildMatchUrl(property.getString()) + ":" + path);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    log.error("Error: {}", ex.getMessage());
                }
            }
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (RepositoryException e) {
            e.printStackTrace();
        } finally {
            session.logout();
        }
    }
    
    private String buildMatchUrl(String input) {
        StringBuilder url = new StringBuilder();
        if (input.charAt(0) != '^') {
            /* Always prepend a start-of-match */
            url.append('^');
        }
        int firstSlash = input.indexOf('/');
        if (firstSlash >= 0 && firstSlash < 2) {
            url.append(input);
        } else {
            url.append('/');
            url.append(input);
        }
        
        return url.toString();
    }

    private void buildMappedUrls() throws org.apache.sling.api.resource.LoginException {
        Session session = null;
        this.resourceResolver = resourceResolverFactory.getResourceResolver(null);
        String queryString = "select * from [sling:mapping]";

    }

    @Activate
    protected void activate(ComponentContext ctx) {
        final Dictionary<?, ?> properties = ctx.getProperties();
        this.customRules = PropertiesUtil.toStringArray(properties.get(CUSTOM_RULES), new String[0]);
        this.mapCustomRules = PropertiesUtil.toBoolean(properties.get(MAP_CUSTOM_RULES), true);
        try {
            this.buildVanityUrls();
        } catch (org.apache.sling.api.resource.LoginException e) {
            log.error("Failure to build vanity URLs in service: {} ", e.getMessage());
            e.printStackTrace();
        }
        this.vanityUrls = this.getVanityUrls();
    }

    private List<String> getVanityUrls() {
        return this.vanityUrls;
    }
}