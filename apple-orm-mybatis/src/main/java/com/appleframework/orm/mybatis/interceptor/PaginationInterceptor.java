package com.appleframework.orm.mybatis.interceptor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.PropertyException;

import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.statement.BaseStatementHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.apache.log4j.Logger;

import com.appleframework.model.page.Pagination;
import com.appleframework.orm.mybatis.parser.SqlParser;
import com.appleframework.orm.mybatis.parser.SqlServer;
import com.appleframework.orm.mybatis.query.PageQuery;
import com.appleframework.orm.mybatis.utils.SystemUtility;

/**
 * 查询分页拦截器，用户拦截SQL，并加上分页的参数和高级查询条件
 * 
 * @author dendy
 * 
 */
@Intercepts({ @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class }) })
public class PaginationInterceptor implements Interceptor {

	private static Logger logger = Logger.getLogger(PaginationInterceptor.class);

	private String dialect = "mysql";

	// 暂时不需要这个参数，现在根据参数类型来判断是否是分页sql
	// private String pageMethodPattern = "";

	@SuppressWarnings("unchecked")
	public Object intercept(Invocation ivk) throws Throwable {
		if (!(ivk.getTarget() instanceof RoutingStatementHandler)) {
			return ivk.proceed();
		}
		RoutingStatementHandler statementHandler = (RoutingStatementHandler) ivk.getTarget();
		BaseStatementHandler delegate 
			= (BaseStatementHandler) SystemUtility.getValueByFieldName(statementHandler, "delegate");
		MappedStatement mappedStatement 
			= (MappedStatement) SystemUtility.getValueByFieldName(delegate, "mappedStatement");

		// BoundSql封装了sql语句
		BoundSql boundSql = delegate.getBoundSql();
		// 获得查询对象
		Object parameterObject = boundSql.getParameterObject();
		Pagination page = null;
		
		// 根据参数类型判断是否是分页方法
		if (parameterObject instanceof PageQuery) {
			PageQuery query = (PageQuery) parameterObject;
			page = query.getDefaultPage();
		}

		else if(parameterObject instanceof Map) {
			Map<String, Object> query = (Map<String, Object>) parameterObject;
			try {
				page = (Pagination)query.get("page");
			} catch (BindingException e) {
				page = null;
			}
			if(null == page)
				return ivk.proceed();
		}
		else {
			return ivk.proceed();
		}
		logger.debug(" beginning to intercept page SQL...");
		Connection connection = (Connection) ivk.getArgs()[0];
		String sql = boundSql.getSql();
		
		// 获取查询数来的总数目
		//String countSql = "SELECT COUNT(0) FROM (" + sql + ") AS tmp ";
		String countSql = null;
		if ("sqlserver".equals(dialect)) {
			countSql = SqlParser.getSmartCountSql(sql);
		}
		else {
			countSql = SqlParser.getSimpleCountSql(sql);
		}
		
		PreparedStatement countStmt = connection.prepareStatement(countSql);
		BoundSql countBS = new BoundSql(mappedStatement.getConfiguration(),
				countSql, boundSql.getParameterMappings(), parameterObject);
		
		//解决foreach不能分页问题
		Map<String, Object> additionalParameters = 
			(Map<String, Object>) SystemUtility.getValueByFieldName(boundSql, "additionalParameters");
		MetaObject metaParameters = (MetaObject) SystemUtility.getValueByFieldName(boundSql, "metaParameters");
		SystemUtility.setValueByFieldName(countBS, "additionalParameters", additionalParameters);
		SystemUtility.setValueByFieldName(countBS, "metaParameters", metaParameters);

		setParameters(countStmt, mappedStatement, countBS, parameterObject);
		ResultSet rs = countStmt.executeQuery();
		int count = 0;
		if (rs.next()) {
			count = rs.getInt(1);
		}
		rs.close();
		countStmt.close();

		// 设置总记录数
		page.setTotalCount(count);
		page.adjustPageNo();
		
		// 设置总页数
		//page.setTotalPage((count + page.getPageSize() - 1) / page.getPageSize());
		// 放到作用于
		//PageContext.getInstance().set(page);

		// 拼装查询参数
		String pageSql = generatePageSql(sql, page);
		SystemUtility.setValueByFieldName(boundSql, "sql", pageSql);
		logger.debug("generated pageSql is : " + pageSql);

		return ivk.proceed();
	}

	/**
	 * setting parameters
	 * 
	 * @param ps
	 * @param mappedStatement
	 * @param boundSql
	 * @param parameterObject
	 * @throws SQLException
	 */
	private void setParameters(PreparedStatement ps,
			MappedStatement mappedStatement, BoundSql boundSql,
			Object parameterObject) throws SQLException {
		ErrorContext.instance().activity("setting parameters").object(mappedStatement.getParameterMap().getId());
		List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
		if (parameterMappings != null) {
			Configuration configuration = mappedStatement.getConfiguration();
			TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
			MetaObject metaObject = parameterObject == null ? null : configuration.newMetaObject(parameterObject);
			for (int i = 0; i < parameterMappings.size(); i++) {
				ParameterMapping parameterMapping = parameterMappings.get(i);
				if (parameterMapping.getMode() != ParameterMode.OUT) {
					Object value;
					String propertyName = parameterMapping.getProperty();
					PropertyTokenizer prop = new PropertyTokenizer(propertyName);
					if (parameterObject == null) {
						value = null;
					} else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
						value = parameterObject;
					} else if (boundSql.hasAdditionalParameter(propertyName)) {
						value = boundSql.getAdditionalParameter(propertyName);
					} else if (propertyName.startsWith(ForEachSqlNode.ITEM_PREFIX)
							&& boundSql.hasAdditionalParameter(prop.getName())) {
						value = boundSql.getAdditionalParameter(prop.getName());
						if (value != null) {
							value = configuration.newMetaObject(value).getValue(propertyName.substring(prop.getName().length()));
						}
					} else {
						value = metaObject == null ? null : metaObject.getValue(propertyName);
					}
					@SuppressWarnings("unchecked")
					TypeHandler<Object> typeHandler = (TypeHandler<Object>) parameterMapping.getTypeHandler();
					if (typeHandler == null) {
						throw new ExecutorException(
								"There was no TypeHandler found for parameter "
										+ propertyName + " of statement "
										+ mappedStatement.getId());
					}
					typeHandler.setParameter(ps, i + 1, value, parameterMapping.getJdbcType());
				}
			}
		}
	}

	/**
	 * 生成Sql语句
	 * 
	 * @param sql
	 * @param page
	 * @return
	 */
	private String generatePageSql(String sql, Pagination page) {
		if (page != null && (dialect != null || !dialect.equals(""))) {
			StringBuffer pageSql = new StringBuffer();
			if ("mysql".equals(dialect)) {
				pageSql.append(sql);
				pageSql.append(" LIMIT " + page.getFirstResult() + "," + page.getPageSize());
			} else if ("oracle".equals(dialect)) {
				pageSql.append("SELECT * FROM (SELECT t.*,ROWNUM r FROM (");
				pageSql.append(sql);
				pageSql.append(") t WHERE r <= ");
				pageSql.append(page.getFirstResult() + page.getPageSize());
				pageSql.append(") WHERE r >");
				pageSql.append(page.getFirstResult());
			} else if ("sqlserver".equals(dialect)) {
				return SqlServer.convertToPageSql(sql, (page.getPageNo() - 1) * page.getPageSize(), page.getPageSize());
			} else {
				
			}
			return pageSql.toString();
		} else {
			return sql;
		}
	}	

	public void setDialect(String dialect) {
		this.dialect = dialect;
	}

	public Object plugin(Object arg0) {
		return Plugin.wrap(arg0, this);
	}

	public void setProperties(Properties p) {
		dialect = p.getProperty("dialect");
		if (dialect == null || dialect.equals("")) {
			try {
				throw new PropertyException("dialect property is not found!");
			} catch (PropertyException e) {
				e.printStackTrace();
			}
		}
		// pageMethodPattern = p.getProperty("pageMethodPattern");
		if (dialect == null || dialect.equals("")) {
			try {
				throw new PropertyException(
						"pageMethodPattern property is not found!");
			} catch (PropertyException e) {
				e.printStackTrace();
			}
		}
	}

}
