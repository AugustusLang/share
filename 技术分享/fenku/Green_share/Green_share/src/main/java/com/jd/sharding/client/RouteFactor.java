package com.jd.sharding.client;

import java.util.HashMap;
import java.util.Map;
public class RouteFactor {
    private Map<String, Object> routeFields = null;
    
    public RouteFactor(String routeFieldKey, Object routeFieldValue) {
    	 routeFields = new HashMap<String, Object>(1);
    	 routeFields.put(routeFieldKey, routeFieldValue);
	}

    public Object getRouteField(String routeFieldKey) {
        if(routeFields != null) {
            return routeFields.get(routeFieldKey);
        }
        return null;
    }
}
