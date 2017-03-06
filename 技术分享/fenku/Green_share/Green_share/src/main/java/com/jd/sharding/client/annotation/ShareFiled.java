package com.jd.sharding.client.annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.TYPE,ElementType.FIELD})
/**
 * 分库字段注解
 * @author liqingyu
 *
 */
 public @interface ShareFiled {
    String value() default "pin"; //为属性提供默认值
 
}