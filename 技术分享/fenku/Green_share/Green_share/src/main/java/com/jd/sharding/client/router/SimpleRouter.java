package com.jd.sharding.client.router;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jd.sharding.client.RouteFactor;
import com.jd.sharding.client.config.ShardConfig;
import com.jd.sharding.client.config.DbConfig;
import com.jd.sharding.client.exception.RouteException;
import com.jd.sharding.client.support.RouteContextConstants;
import com.jd.sharding.client.support.RouteResult;
public class SimpleRouter implements Router<RouteFactor>{
	private final Log log = LogFactory.getLog(SimpleRouter.class);
    public void setShardConfig(ShardConfig shardConfig) {
        this.shardConfig = shardConfig;
    }
    private ShardConfig shardConfig;

    public ShardConfig getShardConfig() {
		return shardConfig;
	}

	public RouteResult doRoute(RouteFactor routeCondition) throws RouteException {
        if (routeCondition == null) {
            throw new IllegalArgumentException("routeCondition or its logicalTableName cann't be null or empty!");
        }
        RouteResult result = new RouteResult();
        try {
        	Map<String, DbConfig> map=shardConfig.getTableConfig();
        	String key=(String)routeCondition.getRouteField(RouteContextConstants.ROUTE_SHARD_FIELD_KEY);
        	String shareKey=map.get("db_Config").getShareKey(key);
        	result.setSharekey(shareKey);
        return result;
        } catch (Throwable e) {
        	log.error("Route error!", e);
            throw new RouteException("Route error!", e);
        }
    }

	public static void main(String[] args) {
		Class c;
		try {
			c = Class.forName("abc");
			Object o = c.getInterfaces();
			Method m = c.getDeclaredMethod("abc", String.class);
			m.invoke(obj, args)
		} catch ( Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
}
