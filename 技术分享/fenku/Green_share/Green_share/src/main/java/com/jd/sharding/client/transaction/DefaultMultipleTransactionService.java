package com.jd.sharding.client.transaction;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.CollectionUtils;

public class DefaultMultipleTransactionService implements MultipleTransactionService, ApplicationContextAware, InitializingBean {

	private ApplicationContext springContext;
	
	private Map<String, Object> transactionManagerConfig;
	
	private Map<String, Object> atomikTransactionManagerConfig;
	
	public void setAllatomikTransactionManager(Map<String, Object> allatomikTransactionManager) {
		this.atomikTransactionManagerConfig = allatomikTransactionManager;
	}

	public void setAllTransactionManager(Map<String, Object> allTransactionManager){
		this.transactionManagerConfig = allTransactionManager;
	}
	
	
	
	private Map<String, PlatformTransactionManager> transactionManagerMap;
	private Map<String, PlatformTransactionManager> atomikTransactionManagerMap;
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.springContext = applicationContext;
	}

	public Map<String, PlatformTransactionManager> getTransactionManagers() {
		if(CollectionUtils.isEmpty(transactionManagerMap)) {
			transactionManagerMap = Collections.emptyMap();
       }
       return Collections.unmodifiableMap(transactionManagerMap);
	}
	
	public Map<String, PlatformTransactionManager> getAtomikTransactionManagers() {
		if(CollectionUtils.isEmpty(atomikTransactionManagerMap)) {
			atomikTransactionManagerMap = Collections.emptyMap();
       }
       return Collections.unmodifiableMap(atomikTransactionManagerMap);
	}
	
	
	
	public void afterPropertiesSet() throws Exception {
		if(CollectionUtils.isEmpty(transactionManagerConfig)&&CollectionUtils.isEmpty(atomikTransactionManagerMap)) {
            throw new IllegalArgumentException("allTransactionManager cann't be null or empty!");
        }
		if(null!=transactionManagerConfig&&transactionManagerConfig.size()>0){
			assemblyTransactionManager();
		}
		if(null!=atomikTransactionManagerConfig&&atomikTransactionManagerConfig.size()>0){
			assemblyAtomikTransactionManager();
		}
	}

	void assemblyTransactionManager(){
		transactionManagerMap = new HashMap<String, PlatformTransactionManager>(transactionManagerConfig.size());
        for(Map.Entry<String, Object> entity : transactionManagerConfig.entrySet()) {
            Object value = entity.getValue();
            if(value instanceof PlatformTransactionManager) {
            	transactionManagerMap.put(entity.getKey(), (PlatformTransactionManager)value);
            } else if(value instanceof String) {
                Object valueBean = springContext.getBean((String)value);
                if(valueBean instanceof PlatformTransactionManager) {
                	transactionManagerMap.put(entity.getKey(), (PlatformTransactionManager)valueBean);
                } else {
                    throw new IllegalArgumentException("illegal value argument,key:" + entity.getKey() +
                            ",a reference or bean name of type PlatformTransactionManager is required!");
                }
            } else {
                throw new IllegalArgumentException("illegal value argument,key:" + entity.getKey() +
                            ",a reference or bean name of type PlatformTransactionManager is required!");
            }
        }
	}
	
	
	void assemblyAtomikTransactionManager(){
		atomikTransactionManagerMap = new HashMap<String, PlatformTransactionManager>(atomikTransactionManagerConfig.size());
        for(Map.Entry<String, Object> entity : atomikTransactionManagerConfig.entrySet()) {
            Object value = entity.getValue();
            if(value instanceof PlatformTransactionManager) {
            	atomikTransactionManagerMap.put(entity.getKey(), (PlatformTransactionManager)value);
            } else if(value instanceof String) {
                Object valueBean = springContext.getBean((String)value);
                if(valueBean instanceof PlatformTransactionManager) {
                	atomikTransactionManagerMap.put(entity.getKey(), (PlatformTransactionManager)valueBean);
                } else {
                    throw new IllegalArgumentException("illegal value argument,key:" + entity.getKey() +
                            ",a reference or bean name of type PlatformTransactionManager is required!");
                }
            } else {
                throw new IllegalArgumentException("illegal value argument,key:" + entity.getKey() +
                            ",a reference or bean name of type PlatformTransactionManager is required!");
            }
        }
		
	}

	
}
