package com.jd.sharding.client.config;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import com.jd.sharding.client.datasource.MultipleDataSourcesService;
import com.jd.sharding.client.rule.RouteRule;
public class DbConfig implements InitializingBean {
	private final Log log = LogFactory.getLog(DbConfig.class);
    /**
     * 库的总数
     */
    private int dbCount;

    /**
     * 所有的库的索引数组
     */
    private String[] dbIndices;
    
    private MultipleDataSourcesService dataSourcesService;

    /**
     * 设置路由规则
     *
     * @param routeRule，路由规则
     */
    public void setRouteRule(RouteRule routeRule) {
        this.routeRule = routeRule;
    }

    public RouteRule getRouteRule() {
        return this.routeRule;
    }

    private RouteRule routeRule;
    
    public String getShareKey(String shareField){
    	//根据shareField获取
    	int index=routeRule.getDbIndex(shareField);
    	return dbIndices[index];
    	
    }

    public void setDbCount(int dbCount) {
        this.dbCount = dbCount;
    }

    public void afterPropertiesSet() throws Exception {
        if (dbCount <= 0) {
            throw new IllegalStateException("illegal dbCount,it must be >0!");
        }
        if (routeRule == null) {
            throw new IllegalArgumentException("routeRule cann't be null!");
        }
        
        dbIndices = createDbIndices(dbCount);
    }
    
    private String[] createDbIndices(int count) {
    	Map<String, String> dataSourceMap = this.dataSourcesService.getDataSources();
    	if(dataSourceMap.size() != count){
    		log.error("dataSourcesService data source count:" + this.dataSourcesService.getDataSources().size() + ", and the dbCount:" + count);
    		 throw new IllegalArgumentException("dataSourcesService data source count and dbCount miscount");
    	}
    	String[] indices = new String[count];
    	ArrayList<String>list=new ArrayList<String>();
    	list.addAll(dataSourceMap.keySet());
    	//排序
    	
    	Collections.sort(list,new Comparator<String>(){

			@Override
			public int compare(String o1, String o2) {
				int a=Integer.valueOf(o1.replace("db", ""));
				int b=Integer.valueOf(o2.replace("db", ""));
				return a-b;
			}});
        for (int i = 0; i < count; i++) {
            indices[i] = list.get(i);
        }
        return indices;
    }

	public void setDataSourcesService(MultipleDataSourcesService dataSourcesService) {
		this.dataSourcesService = dataSourcesService;
	}
}
