package com.appleframework.orm.mybatis.sharding;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.appleframework.orm.mybatis.sharding.TableRouterUtils.TableRouterHolder;

/**
 * 分表路由插件
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class})})
public class TableRouterPlugin implements Interceptor {

	private static Logger logger = LoggerFactory.getLogger(TableRouterPlugin.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //StatementHandler的实现类是RoutingStatementHandler对象
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        //对RoutingStatementHandler对象进行包装,可以通过反射获取其私有属性
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
        //RoutingStatementHandler对象其实是对BaseStatementHandler类的子类的一个代理
        MappedStatement mapper = (MappedStatement) metaStatementHandler.getValue("delegate.mappedStatement");
        //MappedStatement是mapper映射对象,namespace属性的组成包含dao的实现类名以及id
        String mapperId = mapper.getId();
        if (logger.isDebugEnabled()) {
            logger.debug("TableRouterPlugin插件开始解析MappedStatement的id:{}", mapperId);
        }
        //通过mapperId获取dao的类名
        String mapperName = mapper.getId().substring(0, mapperId.lastIndexOf("."));
        if (logger.isDebugEnabled()) {
            logger.debug("TableRouterPlugin插件获取的dao类名:{}", mapperName);
        }
        //dao的类名
        Class<?> clazz = Class.forName(mapperName);
        TableRouter router = clazz.getAnnotation(TableRouter.class);
        if (router != null) {
            logger.info("TableRouterPlugin分表开始,dao:{}", mapperName);
            BoundSql boundSql = (BoundSql) metaStatementHandler.getValue("delegate.boundSql");
            //sql参数
            Object parameterObject = boundSql.getParameterObject();
            List<ParameterMapping> mappings = boundSql.getParameterMappings();
            String sql = boundSql.getSql();
            if (logger.isDebugEnabled()) {
                logger.debug("TableRouterPlugin插件开始分表之前的sql:{}", sql);
                logger.debug("TableRouterPlugin插件开始分表之前的parameterObject:{}", parameterObject);
                logger.debug("TableRouterPlugin插件开始分表之前的mappings:{}", mappings);
                logger.debug("TableRouterPlugin插件开始分表的router:{}", router);
            }
            //从参数中获取分表的后缀值
            String suffix = null ;
            if(StringUtils.isNotBlank(router.tableRoute())){
            	suffix = obtainParam(parameterObject, router.tableRoute());
            }
            if(suffix==null){
            	suffix = obtainParam(parameterObject, router);
                if (suffix == null) {
                    throw new RuntimeException("TableRouterPlugin插件分表路由获取分表后缀为空异常");
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("TableRouterPlugin路由分表的后缀suffix:{}", suffix);
                }
                //判断是否动态分表
                if (router.isAuto()) {
                    suffix = findRealSuffix(router.count(), suffix);
                }
            }
            //生成新的sql
            String newSQL = buildRouterSql(sql, suffix, router);
            if (logger.isDebugEnabled()) {
                logger.debug("TableRouterPlugin路由分表之后的newSql:{}", newSQL);
            }
            metaStatementHandler.setValue("delegate.boundSql.sql", newSQL);
        }
        return invocation.proceed();
    }

    /**
     * 替换sql为路由分表后的sql
     *
     * @return
     */
    private String buildRouterSql(String sql, String suffix, TableRouter router) {
        return SqlRewriter.rewriteSqlTable(sql, router.table(), router.table() + router.decollator() + suffix);
    }

    /**
     * 获取参数中分表路由的值
     *
     * @param params
     * @param router
     * @return
     */
    @SuppressWarnings("unchecked")
    private String obtainParam(Object params, TableRouter router) {
        Object paramVal = null;
        if (logger.isDebugEnabled()) {
            logger.debug("TableRouterPlugin分表参数类型class:{},toString:{}", params.getClass(), params);
        }
        if (params instanceof String || params instanceof Integer || params instanceof Byte || params instanceof Date) {
            paramVal = params;
        } else if (params instanceof Map) {
            paramVal = ((Map<String, Object>) params).get(router.paramKey());
        } else {
            MetaObject paramMeta =SystemMetaObject.forObject(params);
            paramVal = paramMeta.getValue(router.paramKey());
        }
        return paramVal == null ? null : paramVal.toString();
    }
    
    @SuppressWarnings("unchecked")
    private String obtainParam(Object params, String attrbite) {
        Object paramVal = null;
        if (logger.isDebugEnabled()) {
            logger.debug("TableRouterPlugin分表参数类型class:{},toString:{}", params.getClass(), params);
        }
        if (params instanceof String || params instanceof Integer || params instanceof Byte || params instanceof Date) {
            paramVal = null;
        } else if (params instanceof Map) {
            paramVal = ((Map<String, Object>) params).get(attrbite);
        } else {
            MetaObject paramMeta =SystemMetaObject.forObject(params);
            paramVal = paramMeta.getValue(attrbite);
        }
        return paramVal == null ? null : paramVal.toString();
    }

    /**
     * 自动分表路由根据值映射后缀
     *
     * @param count  分表的总数  分表后缀从0开始
     * @param suffix 分表路由的值
     * @return
     */
    public String findRealSuffix(int count, String suffix) {
        if (count > 0) {
            List<Object> shards = new ArrayList<Object>();
            for (int i = 0; i < count; i++) {
                shards.add(i);
            }
            TableRouterUtils tableRouterUtil = TableRouterHolder.instance(shards);
            return tableRouterUtil.getShardInfo(suffix).toString();
        }
        return suffix;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }

}
