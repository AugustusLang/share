package com.jd.sharding.client.util;

import java.util.Collection;
import java.util.Map;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.jd.sharding.client.framework.mybatis.support.CustomSqlSessionTemplate;

public class ShareBeanPostProcessor implements
		ApplicationListener<ContextRefreshedEvent> {
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (event.getApplicationContext().getParent() == null) {
			CustomSqlSessionTemplate template = SpringContextUtil
					.getBean("sqlSessionTemplate");
			Collection<MappedStatement> mappedStatements = template
					.getConfiguration().getMappedStatements();
			if (null != mappedStatements && mappedStatements.size() > 0) {
				Map<String, SqlSessionFactory> maps = template
						.getTargetSqlSessionFactorys();
				if (null != maps) {
					for (String str : maps.keySet()) {
						for (Object msO : mappedStatements) {
							if(msO instanceof MappedStatement){
								MappedStatement ms=(MappedStatement)msO;
								SqlSessionFactory sqlFacotry = maps.get(str);
								Configuration cf = sqlFacotry.getConfiguration();
								Collection<String> exg = cf.getMappedStatementNames();
								if (!exg.contains(ms.getId())) {
									cf.addMappedStatement(ms);
								}
							}
							
						
						}
					}
				}
				Map<String, SqlSessionFactory> atomikMaps = template
						.getAtomikTargetSqlSessionFactorys();
				if (null != atomikMaps) {
					for (String str : atomikMaps.keySet()) {
						for (Object msO  : mappedStatements) {
							if(msO instanceof MappedStatement){
								MappedStatement ms=(MappedStatement)msO;
								SqlSessionFactory sqlFacotry = atomikMaps.get(str);
								Configuration cf = sqlFacotry.getConfiguration();
								Collection<String> exg = cf
										.getMappedStatementNames();
								if (!exg.contains(ms.getId())) {
									cf.addMappedStatement(ms);
								}
							}
							
						}

					}
				}
			}

		}
	}

}
