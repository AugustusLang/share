package com.jd.sharding.client.util;

import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.BeanFactory;

public class ShareProxyFactory extends ProxyFactoryBean{
	private static final long serialVersionUID = 2281717683452678796L;
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		String name[]=new String[1];
		name[0]="shareTxInterceptor";
		super.setInterceptorNames(name);
		super.setBeanFactory(beanFactory);
	}

}
