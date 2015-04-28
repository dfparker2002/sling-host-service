package com.axis41.software.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONStringer;
import org.apache.sling.commons.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axis41.software.service.ExampleNameService;

@Component(immediate = true, metatype = true, label = "sling-host-service Example Servlet")
@Service
@Properties({
    @Property(name = "service.vendor", value = "com.axis41.software"),
    @Property(name = "service.description", value = "com.axis41.software Example Servlet."),
	@Property(name = "sling.servlet.paths", value = "/bin/example/names"),
    @Property(name = "sling.servlet.methods", value = "GET")
})
public class ExampleNameServlet extends SlingSafeMethodsServlet {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Reference
    private ExampleNameService exampleNameService;
    
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        final PrintWriter writer = response.getWriter();
        final String[] namesJson;
        final JSONStringer jsonStringer = new JSONStringer();
        final JSONArray names;
        
        try {
            namesJson = exampleNameService.getNames();
            names = new JSONArray(Arrays.asList(namesJson));        
            jsonStringer.object().key("names").value(names).endObject();
            writer.print(jsonStringer.toString());
        } catch (JSONException ex) {
            logger.error("error parsing json", ex);
            writer.print("{'error': 'error parsing names'}");
        }

        response.setContentType("application/json;charset=utf-8");
    }
}