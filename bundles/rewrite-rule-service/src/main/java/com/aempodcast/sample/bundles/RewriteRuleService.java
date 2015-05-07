package com.aempodcast.sample.bundles;

import java.util.Map;

public interface RewriteRuleService {

    public Map<String, Map<String, String>> getRules();
    public Boolean getMapCustomRules();
    
}
