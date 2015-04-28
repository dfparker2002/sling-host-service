package com.axis41.software.impl;

import java.util.Dictionary;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.PropertyUnbounded;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axis41.software.service.ExampleNameService;

@Component(immediate = true, metatype = true, label = "sling-host-service Example Service")
@Service
@Properties({
    @Property(name = "service.vendor", value = "com.axis41.software"),
    @Property(name = "service.description", value = "com.axis41.software Example OSGI service."),
})
public class ExampleNameServiceImpl implements ExampleNameService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /* example property */
    @Property(label = "Example Names Property", description = "property description goes here", unbounded = PropertyUnbounded.ARRAY)
    private static final String DEFAULT_NAMES = "prop.names";
    private String[] names;

    @Override
    public String[] getNames() {
        return names;
    }

    @Override
    public void setNames(String[] names) {
        this.names = names;
    }
    
    @Activate
    protected void activate(ComponentContext ctx) {
        final Dictionary<?, ?> properties = ctx.getProperties();
        names = PropertiesUtil.toStringArray(properties.get(DEFAULT_NAMES), new String[0]);
    }
}