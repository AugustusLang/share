package com.jd.sharding.client.datasource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.util.CollectionUtils;
public class DefaultMultiDataSourcesService implements MultipleDataSourcesService, ApplicationContextAware, InitializingBean{
    private ApplicationContext springContext;
    private Map<String, String> dataSourceMap;
    private Map<String,String> atomikDataSourceMap;

    public void setDataSourcePool(Map<String, Object> dataSourcePool) {
        this.dataSourcePoolConfig = dataSourcePool;
    }

    private Map<String, Object> dataSourcePoolConfig;
    
    private Map<String, Object> atomikDataSourcePoolConfig;

    public void setAtomikDataSourcePool(
			Map<String, Object> atomikDataSourcePool) {
		this.atomikDataSourcePoolConfig = atomikDataSourcePool;
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.springContext = applicationContext;
    }

    public Map<String, String> getDataSources() {
        if(CollectionUtils.isEmpty(dataSourceMap)) {
             dataSourceMap = Collections.emptyMap();
        }
        return Collections.unmodifiableMap(dataSourceMap);
    }

    
    public Map<String, String> getatomikDataSources() {
        if(CollectionUtils.isEmpty(atomikDataSourceMap)) {
        	atomikDataSourceMap = Collections.emptyMap();
        }
        return Collections.unmodifiableMap(atomikDataSourceMap);
    }
    
    public void afterPropertiesSet(){
    	
        if(CollectionUtils.isEmpty(dataSourcePoolConfig)&&CollectionUtils.isEmpty(atomikDataSourcePoolConfig)) {
            throw new IllegalArgumentException("dataSourcePool cann't be null or empty!");
        }
        if(null!=dataSourcePoolConfig&&dataSourcePoolConfig.size()>0){
        	 assemblyDataSource();
        }
        if(null!=atomikDataSourcePoolConfig&&atomikDataSourcePoolConfig.size()>0){
        	 assemblyAtomikDataSource();
        }
       
      
    }
    
    
    void assemblyDataSource(){
    	dataSourceMap = new HashMap<String, String>(dataSourcePoolConfig.size());
        for(Map.Entry<String, Object> entity : dataSourcePoolConfig.entrySet()) {
            Object value = entity.getValue();
            if(value instanceof DataSource) {
            	throw new IllegalArgumentException("illegal value argument, please use value instend of value-ref!");
            } else if(value instanceof String) {
            	String keyName = (String)value;
                Object valueBean = springContext.getBean(keyName);
                if(valueBean instanceof DataSource) {
                	dataSourceMap.put(entity.getKey(), (String)value);
                	DataSource dataSource = (DataSource)valueBean;
                	if((dataSource instanceof TransactionAwareDataSourceProxy) == false){
                		ShardingDataSources.getInstance().addDataSource(keyName, new TransactionAwareDataSourceProxy(dataSource));   
                	}
                } else {
                    throw new IllegalArgumentException("illegal value argument,key:" + entity.getKey() +
                            ",a reference or bean name of type javax.sql.DataSource is required!");
                }
            } else {
                throw new IllegalArgumentException("illegal value argument,key:" + entity.getKey() +
                            ",a reference or bean name of type javax.sql.DataSource is required!");
            }
        }
    }
    
    void assemblyAtomikDataSource(){

    	atomikDataSourceMap = new HashMap<String, String>(atomikDataSourcePoolConfig.size());
        for(Map.Entry<String, Object> entity : atomikDataSourcePoolConfig.entrySet()) {
            Object value = entity.getValue();
            if(value instanceof DataSource) {
            	throw new IllegalArgumentException("illegal value argument, please use value instend of value-ref!");
            } else if(value instanceof String) {
            	String keyName = (String)value;
                Object valueBean = springContext.getBean(keyName);
                if(valueBean instanceof DataSource) {
                	atomikDataSourceMap.put(entity.getKey(), (String)value);
                	DataSource dataSource = (DataSource)valueBean;
                	if((dataSource instanceof TransactionAwareDataSourceProxy) == false){
                		ShardingDataSources.getInstance().addAtomikDataSource(keyName, new TransactionAwareDataSourceProxy(dataSource));   
                	}
                } else {
                    throw new IllegalArgumentException("illegal value argument,key:" + entity.getKey() +
                            ",a reference or bean name of type javax.sql.DataSource is required!");
                }
            } else {
                throw new IllegalArgumentException("illegal value argument,key:" + entity.getKey() +
                            ",a reference or bean name of type javax.sql.DataSource is required!");
            }
        }
    
    }

}
