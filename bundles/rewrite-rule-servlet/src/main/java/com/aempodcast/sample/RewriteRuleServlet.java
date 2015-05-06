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
import java.util.Map;

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

        response.setContentType("application/json;charset=utf-8");
        Writer w = response.getWriter();
        final Map<String, Map<String, String>> rules;
        final JSONObject rulesObj;
        final JSONWriter jw;

        try {
            rules = ruleService.getRules();
            rulesObj = new JSONObject(rules);
            jw =  new JSONWriter(w)
                    .object()
                    .key("rewriteRules")
                        .value(rulesObj)
                    .endObject();
        } catch (Exception ex) {
            log.error("error generating JSON response: ", ex);
        }
        
        log.info("RewriteRuleServlet");
        
    }

}

