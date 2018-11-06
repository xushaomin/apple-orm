package com.appleframework.orm.mybatis.sharding;

import java.sql.Statement;
import java.util.Properties;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * mybaties执行时间插件统计
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "query", args = {Statement.class,ResultHandler.class})})
public class SqlExecuteTimePlugin implements Interceptor{

	 private static Logger logger = LoggerFactory.getLogger(SqlExecuteTimePlugin.class);

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
         String sql = (String) metaStatementHandler.getValue("delegate.boundSql.sql");
         Object object = null ;
         long startTime = System.currentTimeMillis();
		 object = invocation.proceed();
		 long endTime = System.currentTimeMillis();
		 logger.debug("SqlExecuteTimePlugin##########sql:{},执行时间time:{}",sql,(endTime-startTime));
		 return object ;
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
		//初始化设置属性
	}
}
