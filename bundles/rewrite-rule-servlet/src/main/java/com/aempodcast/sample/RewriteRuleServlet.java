/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.aempodcast.sample;

import java.io.IOException;
import java.io.Writer;
import java.lang.Exception;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.json.io.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import  com.aempodcast.sample.bundles.RewriteRuleService;

@SlingServlet(paths="/bin/vhost/rewrites")
@Properties({
    @Property(name="service.description", value="Rewrite Rule Servlet"),
    @Property(name="service.vendor", value="Axis41")
})
@SuppressWarnings("serial")
public class RewriteRuleServlet extends SlingSafeMethodsServlet {
    
    private final Logger log = LoggerFactory.getLogger(RewriteRuleServlet.class);
    
    @Reference
    RewriteRuleService ruleService;

    @Override
    protected void doGet(SlingHttpServletRequest request,
            SlingHttpServletResponse response) throws ServletException,
            IOException {
        Writer w = response.getWriter();
        final Map<String, Map<String, String>> allRules = ruleService.getRules();

        String responseType = request.getResponseContentType();

        if (responseType == null) {
            responseType = "text/plain";  
        }

        log.debug("Response Type: {}", responseType);
        response.setContentType(responseType+";charset=utf-8");

        if (responseType.equals("application/json")) {
            createJsonResponse(w, allRules);
        } else {
            Map<String, String> rules = new TreeMap<String, String>();
            for(String ruleType : allRules.keySet()) {
                Map<String, String> ruleSet = allRules.get(ruleType);
                for(String match : ruleSet.keySet()) {
                    String target = ruleSet.get(match);
                    if (ruleType.equals("customRules") && ruleService.getMapCustomRules()) {
                        log.trace("Mapping target {}, found {}", target, request.getResourceResolver().map(target));
                        target = request.getResourceResolver().map(target);
                    }
                    rules.put(match, target);
                }
            }
            createTextResponse(w, rules);
        }
    }

    private void createTextResponse(Writer w, Map<String, String> rules) throws IOException {
            for(String key : rules.keySet()) {
                w.write("RewriteRule ");
                w.write(key);
                w.write(" ");
                w.write(rules.get(key));
                w.write(" [PT,L]");
                w.write(10);
            }
    }

    private void createJsonResponse(Writer w, Map<String, Map<String, String>> rules) {
        final JSONObject rulesObj;
        final JSONWriter jw;

        try {
            rulesObj = new JSONObject(rules);
            for (String rwKey : rules.keySet()) {
                JSONObject jobj = new JSONObject(rules.get(rwKey));
                rulesObj.put(rwKey, jobj);
            }
            jw =  new JSONWriter(w)
                    .object()
                    .key("rewriteRules")
                    .value(rulesObj)
                    .endObject();
        } catch (Exception ex) {
            log.error("error generating JSON response: ", ex);
        }
    }

}

