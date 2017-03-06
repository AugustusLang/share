package com.jd.sharding.client.framework.mybatis.support;

import java.util.Stack;
/**
 * 事务调用上线文
 * @author liqingyu
 *
 */
public abstract class CustomerContextHolder {

    private static final ThreadLocal<Stack<String>> contextHolder = new ThreadLocal<Stack<String>>();  

    public static void setContextType(Stack<String> contextType) {  

        contextHolder.set(contextType);  

    }  

      

    public static Stack<String> getContextType() {  

        return contextHolder.get();  

    }  
    public static void clearContextType() {  

        contextHolder.remove();  

    }  

}
