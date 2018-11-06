package com.appleframework.orm.mybatis.sharding;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * mybaties定义mycat读写分离插件
 */
@Intercepts({ @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class }) })
public class MybatiesReadWritePlugin implements Interceptor {

	private static Logger logger = LoggerFactory.getLogger(MybatiesReadWritePlugin.class);

	private static volatile ConcurrentHashMap<String, MasterCacheWrap> cache = new ConcurrentHashMap<>();

	private int pluginType = 0; // 1标识maxscale 0 mycat
	private boolean pluginEnable = true;
	
	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
		MetaObject metaStatementHandler = SystemMetaObject.forObject(statementHandler);
		while (metaStatementHandler.hasGetter("h")) {
			Object object = metaStatementHandler.getValue("h");
			metaStatementHandler = SystemMetaObject.forObject(object);
		}
		// 分离最后一个代理对象的目标类
		while (metaStatementHandler.hasGetter("target")) {
			Object object = metaStatementHandler.getValue("target");
			metaStatementHandler = SystemMetaObject.forObject(object);
		}
		MappedStatement mappedStatement = (MappedStatement) metaStatementHandler.getValue("delegate.mappedStatement");
		String sqlId = mappedStatement.getId();
		logger.debug("MybatiesReadWritePlugin##########sqlId:{}", sqlId);
		int index = sqlId.lastIndexOf("!");
		if (index != -1) {
			sqlId = sqlId.substring(0, index);
		}
		String daoClassName = sqlId.substring(0, sqlId.lastIndexOf('.'));
		String methodName = sqlId.substring(sqlId.lastIndexOf('.') + 1);
		MasterCacheWrap hasMaster = cache.get(daoClassName + "_" + methodName);
		Master master = null;
		if (hasMaster == null) {
			master = isMasterMethod(daoClassName, methodName);
			MasterCacheWrap cacheWrap = null;
			if (master == null) {
				cacheWrap = new MasterCacheWrap(master, false);
			} else {
				cacheWrap = new MasterCacheWrap(master, true);
			}
			cache.put(daoClassName + "_" + methodName, cacheWrap);
		} else if (hasMaster.getHasMaster()) {
			master = hasMaster.getMaster();
		}
		if (master != null && pluginEnable) {
			BoundSql boundSql = (BoundSql) metaStatementHandler.getValue("delegate.boundSql");
			// sql参数
			if (StringUtils.isNotBlank(master.masterKey())) {
				Object parameterObject = boundSql.getParameterObject();
				Object masterValue = obtainParam(parameterObject, master.masterKey());
				if (masterValue != null) {
					String sql = (String) metaStatementHandler.getValue("delegate.boundSql.sql");
					String masterSql = getPluginSql(master, sql);
					if (logger.isDebugEnabled()) {
						logger.debug("MybatiesReadWritePlugin##########masterValue:{},masterSql:{}", masterValue,
								masterSql);
					}
					metaStatementHandler.setValue("delegate.boundSql.sql", masterSql);
				}
			} else {
				String sql = (String) metaStatementHandler.getValue("delegate.boundSql.sql");
				String masterSql = getPluginSql(master, sql);
				if (logger.isDebugEnabled()) {
					logger.debug("MybatiesReadWritePlugin##########masterSql:{}", masterSql);
				}
				metaStatementHandler.setValue("delegate.boundSql.sql", masterSql);
			}
		}
		return invocation.proceed();
	}

	public String getPluginSql(Master master, String sql) {
		String masterSql = "";
		// 1标识maxscale 0 mycat
		if (pluginType == 1) {
			// 强制使用maxscale
			masterSql = sql + Master.MAX_SCALE_MASTER;
		} else if (pluginType == 0) {
			// 强制使用mycat
			masterSql = Master.MYCAT_MASTER + sql;
		} else {
			if (master.position() == PositionType.After) {
				masterSql = sql + master.masterSql();
			} else if (master.position() == PositionType.before) {
				masterSql = master.masterSql() + sql;
			}
		}
		return masterSql;
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {

	}

	@SuppressWarnings("unchecked")
	private Object obtainParam(Object params, String attrbite) {
		Object paramVal = null;
		if (logger.isDebugEnabled()) {
			logger.debug("MybatiesReadWritePlugin读写分离参数类型class:{},toString:{}", params.getClass(), params);
		}
		if (params instanceof String || params instanceof Integer || params instanceof Byte || params instanceof Date
				|| params instanceof Long) {
			paramVal = null;
		} else if (params instanceof Map) {
			paramVal = ((Map<String, Object>) params).get(attrbite);
		} else {
			MetaObject paramMeta = SystemMetaObject.forObject(params);
			paramVal = paramMeta.getValue(attrbite);
		}
		return paramVal;
	}

	public Master isMasterMethod(String daoClassName, String methodName) throws SecurityException, ClassNotFoundException {
		Master masterMethod = null;
		Method[] methods = Class.forName(daoClassName).getMethods();
		for (Method method : methods) {
			if (method.getName().equals(methodName)) {
				Master master = method.getAnnotation(Master.class);
				if (master != null) {
					masterMethod = master;
				}
				break;
			}
		}
		return masterMethod;
	}

	public boolean hasMaster(String daoClassName, String methodName) throws SecurityException, ClassNotFoundException {
		logger.debug("MybatiesReadWritePlugin intercept daoClassName:{},methodName:{}", daoClassName, methodName);
		boolean isSalver = true;
		Method[] methods = Class.forName(daoClassName).getMethods();
		for (Method method : methods) {
			if (method.getName().equals(methodName)) {
				Master master = method.getAnnotation(Master.class);
				if (master != null) {
					isSalver = false;
				}
				break;
			}
		}
		return isSalver;
	}

	public boolean isCheck(String daoClassName, String methodName) throws SecurityException, ClassNotFoundException {
		logger.debug("MybatiesReadWritePlugin intercept daoClassName:{},methodName:{}", daoClassName, methodName);
		boolean isSalver = true;
		Method[] methods = Class.forName(daoClassName).getMethods();
		for (Method method : methods) {
			if (method.getName().equals(methodName)) {
				// 当没有配置master注解时,识别mybaties的注解判断
				Select select = method.getAnnotation(Select.class);
				SelectProvider selectProvider = method.getAnnotation(SelectProvider.class);
				if (select == null && selectProvider == null) {
					isSalver = false;
				}
				break;
			}
		}
		return isSalver;
	}

	/**
	 * 通过dao和方法判断是否需要主从库切换
	 * 
	 * @param daoClassName
	 * @param methodName
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 */
	public boolean hasMasterAndCheck(String daoClassName, String methodName) throws SecurityException, ClassNotFoundException {
		logger.debug("MybatiesReadWritePlugin intercept daoClassName:{},methodName:{}", daoClassName, methodName);
		boolean isSalver = true;
		Method[] methods = Class.forName(daoClassName).getMethods();
		for (Method method : methods) {
			if (method.getName().equals(methodName)) {
				// 先通过注解判断是否切换从库
				Master master = method.getAnnotation(Master.class);
				if (master != null) {
					isSalver = false;
				} else {
					// 当没有配置master注解时,识别mybaties的注解判断
					Select select = method.getAnnotation(Select.class);
					SelectProvider selectProvider = method.getAnnotation(SelectProvider.class);
					if (select == null && selectProvider == null) {
						isSalver = false;
					}
				}
				break;
			}
		}
		return isSalver;
	}
	
	

	public void setDatabasePlugin(String databasePlugin) {
		// 1标识maxscale 0 mycat
		if(StringUtils.isNotEmpty(databasePlugin)) {
			if(databasePlugin.equals("mycat")) {
				pluginType = 0;
			}
			else if(databasePlugin.equals("maxscale")) {
				pluginType = 1;
			}
			else {
				logger.error("The databasePlugin {} is Error !!!", databasePlugin);
			}
		}
	}

	public void setPluginEnable(boolean pluginEnable) {
		this.pluginEnable = pluginEnable;
	}

	public static class MasterCacheWrap {
		
		private Master master;
		private Boolean hasMaster;

		/**
		 * @param master
		 * @param hasMaster
		 */
		public MasterCacheWrap(Master master, Boolean hasMaster) {
			super();
			this.master = master;
			this.hasMaster = hasMaster;
		}

		/**
		 * @return the master
		 */
		public Master getMaster() {
			return master;
		}

		/**
		 * @param master the master to set
		 */
		public void setMaster(Master master) {
			this.master = master;
		}

		/**
		 * @return the hasMaster
		 */
		public Boolean getHasMaster() {
			return hasMaster;
		}

		/**
		 * @param hasMaster the hasMaster to set
		 */
		public void setHasMaster(Boolean hasMaster) {
			this.hasMaster = hasMaster;
		}
	}
}