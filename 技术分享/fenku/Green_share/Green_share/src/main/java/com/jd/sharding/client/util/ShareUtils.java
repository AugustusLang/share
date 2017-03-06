package com.jd.sharding.client.util;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;

import com.jd.sharding.client.annotation.ShareFiled;

public class ShareUtils {
	private static final Log log = LogFactory
			.getLog(ShareUtils.class);

	public static String getShareField(final Object[] args, Method mImp) throws Exception {
		String shareFiled = "";
		for (Object arg : args) {
			if (arg instanceof Map) {
				Map map = (Map) arg;
				Object o = map.get("ShareFiled");
				if (null != o) {
					shareFiled = o.toString();
					break;
				}

			} else if (arg instanceof String || arg instanceof Long
					|| arg instanceof Integer) {
				Annotation[][] ans = mImp.getParameterAnnotations();
				for (int k = 0; k < ans.length; k++) {
					for (int h = 0; h < ans[k].length; h++) {
						if (ans[k][h] instanceof ShareFiled) {
							shareFiled = args[k].toString();
							break;
						}
					}
				}
				;
			} else if (arg instanceof Object) {
				// 对象属性是否含有注解
				Field[] fields = arg.getClass().getDeclaredFields();
				if (null != fields && fields.length > 0) {
					// 判断字段前是否有分片注解
					for (Field field : fields) {
						Annotation[] filedAnno = field.getAnnotations();
						if (null != filedAnno && filedAnno.length > 0) {
							for (Annotation anno : filedAnno) {
								if (anno instanceof ShareFiled) {
									// 获取字段值
									PropertyDescriptor descriptor = BeanUtils
											.getPropertyDescriptor(
													arg.getClass(),
													field.getName());
									if (descriptor != null
											&& descriptor.getReadMethod() != null) {
										Object fieldValue = descriptor
												.getReadMethod().invoke(arg);
										if (null != fieldValue) {
											shareFiled = fieldValue.toString();
										}
										break;
									}
								}
							}
						}
						if (StringUtils.isNotBlank(shareFiled)) {
							break;
						}
					}
				}
			}
			if (StringUtils.isNotBlank(shareFiled)) {
				break;
			}
		}
		return shareFiled;
	}

	
	public static String getShareFiled(Object[] args) throws Exception{
		String filed="";
		for (int i = 0, j = args.length; i < j; i++) {
				if(args[i] instanceof Map){
					//获取属性值
					Map map=(Map) args[i];
					try {
						Object o=map.get("ShareFiled");
						if(null!=o){
							filed=o.toString();
							return filed;
						}
					} catch (Exception e) {
						log.error("getShareFiled error", e);
					}
				
				}
				
				else {
					//获取对象注解，获取
					Field[] fileds=	args[i].getClass().getDeclaredFields();
					if(null!=fileds&&fileds.length>0){
						for(int k=0;k<fileds.length;k++){
							Annotation[] anns=fileds[k].getAnnotations();
							for(Annotation ant:anns){
								if(ant instanceof ShareFiled){
									//获取改字段值
									String filedName=fileds[k].getName();
									Object o=BeanPropertyAccessUtil.getPropertyValue(filedName, args[i]);
									if(null!=o){
										filed=o.toString();
										return filed;
									}
								}
							}
						}
					}
				}	
		}
		return filed;
	}

}
