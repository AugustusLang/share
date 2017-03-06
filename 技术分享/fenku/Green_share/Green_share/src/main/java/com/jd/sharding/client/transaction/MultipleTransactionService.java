package com.jd.sharding.client.transaction;

import java.util.Map;

import org.springframework.transaction.PlatformTransactionManager;

public interface MultipleTransactionService {
	
	public Map<String, PlatformTransactionManager> getTransactionManagers();
	public Map<String, PlatformTransactionManager> getAtomikTransactionManagers();
	
}
