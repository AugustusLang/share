package com.jd.sharding.client.framework.mybatis.support;

import static com.jd.sharding.client.util.BeanPropertyAccessUtil.setPropertyValue;
import static java.lang.reflect.Proxy.newProxyInstance;
import static org.apache.ibatis.reflection.ExceptionUtil.unwrapThrowable;
import static org.mybatis.spring.SqlSessionUtils.closeSqlSession;
import static org.mybatis.spring.SqlSessionUtils.getSqlSession;
import static org.mybatis.spring.SqlSessionUtils.isSqlSessionTransactional;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.MyBatisExceptionTranslator;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.util.Assert;

import com.jd.sharding.client.RouteFactor;
import com.jd.sharding.client.enums.ShareEnum.TRANTYPE;
import com.jd.sharding.client.exception.RouteException;
import com.jd.sharding.client.router.Router;
import com.jd.sharding.client.support.RouteContextConstants;
import com.jd.sharding.client.support.RouteResult;
import com.jd.sharding.client.util.ShareUtils;
/**
 * 
 * @author liqingyu
 *
 */
public class CustomSqlSessionTemplate extends SqlSessionTemplate {
	@Autowired
	private Router<RouteFactor> router;
	private final SqlSessionFactory sqlSessionFactory;

	private final ExecutorType executorType;

	private final SqlSession sqlSessionProxy;

	private final PersistenceExceptionTranslator exceptionTranslator;

	private Map<String, SqlSessionFactory> targetSqlSessionFactorys;
	
	public Map<String, SqlSessionFactory> getTargetSqlSessionFactorys() {
		return targetSqlSessionFactorys;
	}

	private Map<String, SqlSessionFactory> atomikTargetSqlSessionFactorys;

	public Map<String, SqlSessionFactory> getAtomikTargetSqlSessionFactorys() {
		return atomikTargetSqlSessionFactorys;
	}

	public void setAtomikTargetSqlSessionFactorys(
			Map<String, SqlSessionFactory> atomikTargetSqlSessionFactorys) {
		this.atomikTargetSqlSessionFactorys = atomikTargetSqlSessionFactorys;
	}

	private SqlSessionFactory defaultTargetSqlSessionFactory;

	public void setTargetSqlSessionFactorys(
			Map<String, SqlSessionFactory> targetSqlSessionFactorys) {

		this.targetSqlSessionFactorys = targetSqlSessionFactorys;

	}

	public void setDefaultTargetSqlSessionFactory(
			SqlSessionFactory defaultTargetSqlSessionFactory) {
		this.defaultTargetSqlSessionFactory = defaultTargetSqlSessionFactory;

	}

	public CustomSqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {

		this(sqlSessionFactory, sqlSessionFactory.getConfiguration()
				.getDefaultExecutorType());

	}

	public CustomSqlSessionTemplate(SqlSessionFactory sqlSessionFactory,
			ExecutorType executorType) {

		this(sqlSessionFactory, executorType, new MyBatisExceptionTranslator(
				sqlSessionFactory.getConfiguration()

				.getEnvironment().getDataSource(), true));

	}

	public CustomSqlSessionTemplate(SqlSessionFactory sqlSessionFactory,
			ExecutorType executorType,

			PersistenceExceptionTranslator exceptionTranslator) {

		super(sqlSessionFactory, executorType, exceptionTranslator);

		this.sqlSessionFactory = sqlSessionFactory;

		this.executorType = executorType;

		this.exceptionTranslator = exceptionTranslator;

		this.sqlSessionProxy = (SqlSession) newProxyInstance(

		SqlSessionFactory.class.getClassLoader(),

		new Class[] { SqlSession.class },

		new SqlSessionInterceptor());

		this.defaultTargetSqlSessionFactory = sqlSessionFactory;

	}

	public SqlSessionFactory getSqlSessionFactory(String shareField) {
		SqlSessionFactory targetSqlSessionFactory = null;
		Stack<String> transactionStack = CustomerContextHolder.getContextType();
		if(null==transactionStack){
			if(null!=targetSqlSessionFactorys&&targetSqlSessionFactorys.size()>0){
				 targetSqlSessionFactory = targetSqlSessionFactorys
							.get(shareField);
			}else if(null!=atomikTargetSqlSessionFactorys&&atomikTargetSqlSessionFactorys.size()>0){
				targetSqlSessionFactory = atomikTargetSqlSessionFactorys.get(shareField);
			}
			
		}else{
			String transactionType=transactionStack.peek();
			if(TRANTYPE.SINGLE.getName().equals(transactionType)){
			 targetSqlSessionFactory = targetSqlSessionFactorys
						.get(shareField);
				
			}else if(TRANTYPE.ATOMIK.getName().equals(transactionType)){
			 targetSqlSessionFactory = atomikTargetSqlSessionFactorys.get(shareField);
			}
		}
		if (targetSqlSessionFactory != null) {

			return targetSqlSessionFactory;

		} else if (defaultTargetSqlSessionFactory != null) {

			return defaultTargetSqlSessionFactory;

		} else {

			Assert.notNull(
					targetSqlSessionFactorys,
					"Property 'targetSqlSessionFactorys' or 'defaultTargetSqlSessionFactory' are required");

			Assert.notNull(
					defaultTargetSqlSessionFactory,
					"Property 'defaultTargetSqlSessionFactory' or 'targetSqlSessionFactorys' are required");

		}

		return this.sqlSessionFactory;

	}

	@Override
	public SqlSessionFactory getSqlSessionFactory() {
	if (defaultTargetSqlSessionFactory != null) {
			return defaultTargetSqlSessionFactory;

		} else {
			Assert.notNull(
					targetSqlSessionFactorys,
					"Property 'targetSqlSessionFactorys' or 'defaultTargetSqlSessionFactory' are required");

			Assert.notNull(
					defaultTargetSqlSessionFactory,
					"Property 'defaultTargetSqlSessionFactory' or 'targetSqlSessionFactorys' are required");

		}

		return this.sqlSessionFactory;

	}

	@Override
	public Configuration getConfiguration() {

		return this.getSqlSessionFactory().getConfiguration();

	}

	public ExecutorType getExecutorType() {

		return this.executorType;

	}

	public PersistenceExceptionTranslator getPersistenceExceptionTranslator() {

		return this.exceptionTranslator;

	}

	/**
	 * 
	 * {@inheritDoc}
	 */

	public <T> T selectOne(String statement) {

		return this.sqlSessionProxy.<T> selectOne(statement);

	}

	/**
	 * 
	 * {@inheritDoc}
	 */

	public <T> T selectOne(String statement, Object parameter) {

		return this.sqlSessionProxy.<T> selectOne(statement, parameter);

	}

	/**
	 * 
	 * {@inheritDoc}
	 */

	public <K, V> Map<K, V> selectMap(String statement, String mapKey) {

		return this.sqlSessionProxy.<K, V> selectMap(statement, mapKey);

	}

	/**
	 * 
	 * {@inheritDoc}
	 */

	public <K, V> Map<K, V> selectMap(String statement, Object parameter,
			String mapKey) {

		return this.sqlSessionProxy.<K, V> selectMap(statement, parameter,
				mapKey);

	}

	/**
	 * 
	 * {@inheritDoc}
	 */

	public <K, V> Map<K, V> selectMap(String statement, Object parameter,
			String mapKey, RowBounds rowBounds) {

		return this.sqlSessionProxy.<K, V> selectMap(statement, parameter,
				mapKey, rowBounds);

	}

	/**
	 * 
	 * {@inheritDoc}
	 */

	public <E> List<E> selectList(String statement) {

		return this.sqlSessionProxy.<E> selectList(statement);

	}

	/**
	 * 
	 * {@inheritDoc}
	 */

	public <E> List<E> selectList(String statement, Object parameter) {

		return this.sqlSessionProxy.<E> selectList(statement, parameter);

	}

	/**
	 * 
	 * {@inheritDoc}
	 */

	public <E> List<E> selectList(String statement, Object parameter,
			RowBounds rowBounds) {

		return this.sqlSessionProxy.<E> selectList(statement, parameter,
				rowBounds);

	}

	/**
	 * 
	 * {@inheritDoc}
	 */

	public void select(String statement, ResultHandler handler) {

		this.sqlSessionProxy.select(statement, handler);

	}

	/**
	 * 
	 * {@inheritDoc}
	 */

	public void select(String statement, Object parameter, ResultHandler handler) {

		this.sqlSessionProxy.select(statement, parameter, handler);

	}

	/**
	 * 
	 * {@inheritDoc}
	 */

	public void select(String statement, Object parameter, RowBounds rowBounds,
			ResultHandler handler) {

		this.sqlSessionProxy.select(statement, parameter, rowBounds, handler);

	}

	/**
	 * 
	 * {@inheritDoc}
	 */

	public int insert(String statement) {

		return this.sqlSessionProxy.insert(statement);

	}

	/**
	 * 
	 * {@inheritDoc}
	 */

	public int insert(String statement,Object parameter) {
		return this.sqlSessionProxy.insert(statement, parameter);

	}

	/**
	 * 
	 * {@inheritDoc}
	 */

	public int update(String statement) {

		return this.sqlSessionProxy.update(statement);

	}

	/**
	 * 
	 * {@inheritDoc}
	 */

	public int update(String statement, Object parameter) {

		return this.sqlSessionProxy.update(statement, parameter);

	}

	/**
	 * 
	 * {@inheritDoc}
	 */

	public int delete(String statement) {

		return this.sqlSessionProxy.delete(statement);

	}

	/**
	 * 
	 * {@inheritDoc}
	 */

	public int delete(String statement, Object parameter) {

		return this.sqlSessionProxy.delete(statement, parameter);

	}

	/**
	 * 
	 * {@inheritDoc}
	 */

	public <T> T getMapper(Class<T> type) {

		return getConfiguration().getMapper(type, this);

	}

	/**
	 * 
	 * {@inheritDoc}
	 */

	public void commit() {

		throw new UnsupportedOperationException(
				"Manual commit is not allowed over a Spring managed SqlSession");

	}

	/**
	 * 
	 * {@inheritDoc}
	 */

	public void commit(boolean force) {

		throw new UnsupportedOperationException(
				"Manual commit is not allowed over a Spring managed SqlSession");

	}

	/**
	 * 
	 * {@inheritDoc}
	 */

	public void rollback() {

		throw new UnsupportedOperationException(
				"Manual rollback is not allowed over a Spring managed SqlSession");

	}

	/**
	 * 
	 * {@inheritDoc}
	 */

	public void rollback(boolean force) {

		throw new UnsupportedOperationException(
				"Manual rollback is not allowed over a Spring managed SqlSession");

	}

	/**
	 * 
	 * {@inheritDoc}
	 */

	public void close() {

		throw new UnsupportedOperationException(
				"Manual close is not allowed over a Spring managed SqlSession");

	}

	/**
	 * 
	 * {@inheritDoc}
	 */

	public void clearCache() {

		this.sqlSessionProxy.clearCache();

	}

	/**
	 * 
	 * {@inheritDoc}
	 */

	public Connection getConnection() {

		return this.sqlSessionProxy.getConnection();

	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @since 1.0.2
	 */

	public List<BatchResult> flushStatements() {

		return this.sqlSessionProxy.flushStatements();

	}

	/**
	 * 
	 * Proxy needed to route MyBatis method calls to the proper SqlSession got
	 * from Spring's Transaction Manager It also
	 * 
	 * unwraps exceptions thrown by {@code Method#invoke(Object, Object...)} to
	 * pass a {@code PersistenceException} to
	 * 
	 * the {@code PersistenceExceptionTranslator}.
	 */

	private class SqlSessionInterceptor implements InvocationHandler {

		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			//获取分片字段
			String filed=ShareUtils.getShareFiled(args);
			
			if(StringUtils.isBlank(filed)){
				throw new RuntimeException("shareFiled null error");
			}
			//获取路由信息
			RouteFactor routeFactor = new RouteFactor(RouteContextConstants.ROUTE_SHARD_FIELD_KEY,
					filed);
			RouteResult routerResult = router.doRoute(routeFactor);
			String shareKey = routerResult.getSharekey();
			
			final SqlSession sqlSession = getSqlSession(

			CustomSqlSessionTemplate.this.getSqlSessionFactory(shareKey),

			CustomSqlSessionTemplate.this.executorType,

			CustomSqlSessionTemplate.this.exceptionTranslator);

			try {

				Object result = method.invoke(sqlSession, args);

				if (!isSqlSessionTransactional(sqlSession,
						CustomSqlSessionTemplate.this.getSqlSessionFactory(shareKey))) {

					// force commit even on non-dirty sessions because some
					// databases require

					// a commit/rollback before calling close()

					sqlSession.commit(true);

				}

				return result;

			} catch (Throwable t) {

				Throwable unwrapped = unwrapThrowable(t);

				if (CustomSqlSessionTemplate.this.exceptionTranslator != null
						&& unwrapped instanceof PersistenceException) {

					Throwable translated = CustomSqlSessionTemplate.this.exceptionTranslator

							.translateExceptionIfPossible((PersistenceException) unwrapped);

					if (translated != null) {

						unwrapped = translated;

					}

				}

				throw unwrapped;

			} finally {

				closeSqlSession(sqlSession,
						CustomSqlSessionTemplate.this.getSqlSessionFactory(shareKey));

			}

		}

	}

	
	/**
	 * 通过反射将物理的表名回写到传入的parameter对象对应的属性中
	 * 
	 * @param parameterObj
	 *            ,the parameter object
	 * @param tableNameField
	 *            ,parameter name of the table name passed to ibatis engine
	 * @param physicalTableName
	 *            ,the true name after the logical table is routed
	 */
	private void rewriteTableName(final Object parameterObj,
			final String tableNameField, final String physicalTableName)
			throws RouteException {
		try {
			if (parameterObj instanceof Map) {
				((Map) parameterObj).put(tableNameField, physicalTableName);
			} else if (parameterObj != null) {
				setPropertyValue(tableNameField, physicalTableName,
						parameterObj);
			}
		} catch (Exception e) {
			throw new RouteException("rewrite table name error!", e);
		}
	}

}