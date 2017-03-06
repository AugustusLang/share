package com.jd.sharding.client.datasource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

public class ShardingDataSources {

	private static ShardingDataSources shardingDataSources = new ShardingDataSources();
	
	private Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<String, DataSource>();
	private Map<String, DataSource> atomikDataSourceMap = new ConcurrentHashMap<String, DataSource>();
	
	private ShardingDataSources(){
		
	}
	
	public static ShardingDataSources getInstance(){
		return shardingDataSources;
	}
	
	public synchronized void addDataSource(String dataSourceName, DataSource dataSource){
		if(dataSourceMap.containsKey(dataSourceName) == false){
			dataSourceMap.put(dataSourceName, dataSource);
		}
	}
	
	public synchronized void addAtomikDataSource(String dataSourceName, DataSource dataSource){
		if(atomikDataSourceMap.containsKey(dataSourceName) == false){
			atomikDataSourceMap.put(dataSourceName, dataSource);
		}
	}
	
	public DataSource getDataSource(String dataSourceName){
		return dataSourceMap.get(dataSourceName);
	}
	
	public DataSource getAtomikDataSource(String dataSourceName){
		return atomikDataSourceMap.get(dataSourceName);
	}
}
