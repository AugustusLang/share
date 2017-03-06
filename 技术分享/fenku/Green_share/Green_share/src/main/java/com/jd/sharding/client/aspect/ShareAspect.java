package com.jd.sharding.client.aspect;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Stack;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import com.jd.sharding.client.RouteFactor;
import com.jd.sharding.client.annotation.ShareTransaction;
import com.jd.sharding.client.enums.ShareEnum.TRANTYPE;
import com.jd.sharding.client.framework.mybatis.support.CustomerContextHolder;
import com.jd.sharding.client.manager.TransactionTemplateUtils;
import com.jd.sharding.client.router.Router;
import com.jd.sharding.client.support.RouteContextConstants;
import com.jd.sharding.client.support.RouteResult;
import com.jd.sharding.client.transaction.MultipleTransactionService;
import com.jd.sharding.client.util.ShareUtils;

@Aspect
public class ShareAspect {
	private final Log log = LogFactory.getLog(ShareAspect.class);
	@Autowired
	private MultipleTransactionService transactionManagerService;
	@Autowired
	private Router<RouteFactor> router;

	@Pointcut("@annotation(com.jd.sharding.client.annotation.ShareTransaction)")
	public void pointCut() {
	}

	@Around("pointCut()")
	public Object around(ProceedingJoinPoint pjp) throws Throwable {
		Object obj = pjp.getThis();
		Object targetO = pjp.getTarget();
		Object[] args = pjp.getArgs();
		Method handleMethod = getMethod(pjp);
		if (Object.class.equals(handleMethod.getDeclaringClass())) {
			return handleMethod.invoke(obj, args);
		}

		boolean isTranction = false;
		try {
			Annotation[] annotations = handleMethod.getAnnotations();
			ShareTransaction thisAnn = null;
			int count = 0;
			if (null != annotations && annotations.length > 0) {
				for (Annotation ann : annotations) {
					if (ann instanceof ShareTransaction) {
						thisAnn = ((ShareTransaction) ann);
						count++;
						isTranction = true;
					}
				}
			}

			if (count > 1) {
				throw new RuntimeException("重复事务注解异常");
			}
			if (args == null || args.length == 0) {
				log.error("调用方法[" + handleMethod.getName()
						+ "]的数据库处理异常：请求参数为空！");
				return null;
			}
			String shareFiled = ShareUtils.getShareField(args, handleMethod);
			if(StringUtils.isBlank(shareFiled)){
				throw new RuntimeException(handleMethod.getName()
						+ "缺少分库字段");
			}
			// 2.根据切分字段hash 获取事物源
			RouteFactor routeFactor = new RouteFactor(
					RouteContextConstants.ROUTE_SHARD_FIELD_KEY, shareFiled);
			RouteResult routerResult = router.doRoute(routeFactor);
			String shareKey = routerResult.getSharekey();
			// 将物理表反
			if (StringUtils.isBlank(shareKey)) {
				// 抛出异常,若是不分库分表，不继承改类
				throw new RuntimeException(handleMethod.getName()
						+ " InvocationTargetException");
			}

			if (isTranction) {
				Stack<String> transactionStack = CustomerContextHolder
						.getContextType();
				if (null == transactionStack) {
					transactionStack = new Stack<String>();

				}
				PlatformTransactionManager transManager = null;
				String annType = thisAnn.type();
				if (TRANTYPE.SINGLE.getName().equals(annType)) {
					transactionStack.add(annType);
					transManager = transactionManagerService
							.getTransactionManagers().get(shareKey);
				} else if (TRANTYPE.ATOMIK.getName().equals(annType)) {
					transactionStack.add(annType);
					transManager = transactionManagerService
							.getAtomikTransactionManagers().get(shareKey);
				}
				if (null == transManager) {
					throw new RuntimeException("不支持的事务类型");
				}
				CustomerContextHolder.setContextType(transactionStack);
				if (transactionStack.contains("single")
						&& transactionStack.contains("atomik")) {
					throw new RuntimeException("不支持的混合事务");
				}
				return this.doWithTransaction(targetO, handleMethod, args,
						transManager, thisAnn, pjp);
			} else {
				return this.doWithoutTransaction(targetO, handleMethod, args,
						pjp);
			}
		} catch (Throwable e) {
			log.error("handleDataBase error", e);
			throw new RuntimeException(handleMethod.getName(), e);
		} finally {
			if (isTranction) {
				Stack<String> transactionStack = CustomerContextHolder
						.getContextType();
				if (null != transactionStack) {
					transactionStack.pop();
					if (transactionStack.size() == 0) {
						CustomerContextHolder.clearContextType();
					} else {
						CustomerContextHolder.setContextType(transactionStack);
					}
				}

			}

		}
	}

	/**
	 * 获取方法
	 * 
	 * @param jp
	 * @return
	 * @throws Exception
	 */
	private Method getMethod(JoinPoint jp) throws Exception {
		MethodSignature msig = (MethodSignature) jp.getSignature();
		Method method = msig.getMethod();
		return method;
	}

	public void setTransactionManagerService(
			MultipleTransactionService transactionManagerService) {
		this.transactionManagerService = transactionManagerService;
	}

	public Router<RouteFactor> getRouter() {
		return router;
	}

	public void setRouter(Router<RouteFactor> router) {
		this.router = router;
	}

	public Object doWithTransaction(final Object obj,
			final Method handleMethod, final Object[] args,
			PlatformTransactionManager transManager, ShareTransaction ann,
			final ProceedingJoinPoint jp) {
		TransactionTemplate template = null;
		String type = ann.type();
		if (TRANTYPE.SINGLE.getName().equals(type)) {
			template = TransactionTemplateUtils.getTransactionTemplate(
					transManager, ann.propagation().value(), ann.isolation()
							.value());
		} else if (TRANTYPE.ATOMIK.getName().equals(type)) {
			template = TransactionTemplateUtils.getTransactionTemplate(
					transManager, ann.propagation().value(), ann.isolation()
							.value());
		}
		Object result = template.execute(new TransactionCallback<Object>() {
			@Override
			public Object doInTransaction(TransactionStatus status) {
				try {
					Object tranO = jp.proceed();
					return tranO;
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(handleMethod.getName()
							+ " IllegalAccessException", e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(handleMethod.getName()
							+ " IllegalAccessException", e);
				} catch (InvocationTargetException e) {
					throw new RuntimeException(handleMethod.getName()
							+ " IllegalAccessException", e);
				} catch (Throwable t) {
					log.error(handleMethod.getName() + " error!", t);
					throw new RuntimeException(handleMethod.getName(), t);
				}
			}
		});
		return result;
	}

	public Object doWithoutTransaction(final Object obj,
			final Method handleMethod, final Object[] args,
			ProceedingJoinPoint jp) {
		Object result = null;
		try {
			result = jp.proceed();
		} catch (IllegalArgumentException e) {
			log.error(handleMethod.getName() + " IllegalArgumentException", e);
		} catch (IllegalAccessException e) {
			log.error(handleMethod.getName() + " IllegalAccessException", e);
		} catch (InvocationTargetException e) {
			log.error(handleMethod.getName() + " InvocationTargetException", e);
		} catch (Throwable t) {
			log.error(handleMethod.getName() + " error!", t);
		}
		return result;
	}

}