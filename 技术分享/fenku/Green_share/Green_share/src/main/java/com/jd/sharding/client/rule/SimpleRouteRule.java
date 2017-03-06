package com.jd.sharding.client.rule;
import com.jd.sharding.client.util.MurmurHash;
public class SimpleRouteRule implements RouteRule<Object> {
    private int dbsCount;
    public SimpleRouteRule(int dbsCount) {
        if(dbsCount <= 0) {
            throw new IllegalArgumentException("dbsCount and tablesCount must be both positive!");
        }
        this.dbsCount = dbsCount;
    }
    
    public int getDbIndex(Object routeFactor) {
    	if(routeFactor instanceof Integer || routeFactor instanceof Long || routeFactor instanceof String){
    		return (int)(Math.abs(MurmurHash.hash(String.valueOf(routeFactor))) % (dbsCount));
    	}
        throw new IllegalArgumentException("Unsupported RouteFactor parameter type! the support RouteFactor parameter is Integer, Long and String.");
    }
}
