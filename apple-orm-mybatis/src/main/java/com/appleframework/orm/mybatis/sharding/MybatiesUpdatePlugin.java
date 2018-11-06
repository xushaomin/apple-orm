package com.appleframework.orm.mybatis.sharding;

import java.util.Properties;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * mybaties插件
 */
@Intercepts({
	@Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class})	
})
public class MybatiesUpdatePlugin implements Interceptor{

	private Logger logger = LoggerFactory.getLogger(MybatiesUpdatePlugin.class);
	
	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		Object target = invocation.getTarget() ;
		logger.info("[MybatiesUpdatePlugin] method=" + invocation.getMethod().getName());
		((Executor)target).getTransaction().getConnection().setReadOnly(false);
		return invocation.proceed();
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
		
	}
}
