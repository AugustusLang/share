package com.jd.sharding.client.util;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
public class BeanPropertyAccessUtil {
    public static void setPropertyValue(final String propertyName, final Object propertyValue, final Object object) throws InvocationTargetException, IllegalAccessException {
        if (StringUtils.isBlank(propertyName) || object == null) {
            throw new IllegalArgumentException("'propertyName' cann't be null or empty,'object' cann't be null!");
        }
        PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(object.getClass(), propertyName);
        if (descriptor != null && descriptor.getWriteMethod() != null) {
            descriptor.getWriteMethod().invoke(object, propertyValue);
        }
    }
    public static Object getPropertyValue(final String propertyName, final Object object) throws InvocationTargetException, IllegalAccessException {
        if (StringUtils.isBlank(propertyName) || object == null) {
            throw new IllegalArgumentException("'propertyName' cann't be null or empty,'object' cann't be null!");
        }
        PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(object.getClass(), propertyName);
        if (descriptor != null && descriptor.getReadMethod() != null) {
            return descriptor.getReadMethod().invoke(object);
        }
        return null;
    }
    
    
    

}
