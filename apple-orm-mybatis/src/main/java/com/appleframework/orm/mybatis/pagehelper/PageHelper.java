/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 abel533@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.appleframework.orm.mybatis.pagehelper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.DataSource;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/**
 * Mybatis - ͨ�÷�ҳ������
 *
 * @author liuzh/abel533/isea533
 * @version 3.3.0
 *          ��Ŀ��ַ : http://git.oschina.net/free/Mybatis_PageHelper
 */

@Intercepts(@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}))
public class PageHelper implements Interceptor {
    
	//sql������
    private SqlUtil sqlUtil;
    
    //���Բ�����Ϣ
    private Properties properties;
    
    //���ö���ʽ
    private SqlUtilConfig sqlUtilConfig;
    
    //�Զ���ȡdialect,���û��setProperties��setSqlUtilConfig��Ҳ������������
    private boolean autoDialect = true;
    
    //����ʱ�Զ���ȡdialect
    private boolean autoRuntimeDialect;
    
    //������Դʱ����ȡjdbcurl���Ƿ�ر�����Դ
    private boolean closeConn = true;
    
    //����
    private Map<String, SqlUtil> urlSqlUtilMap = new ConcurrentHashMap<String, SqlUtil>();
    
    private ReentrantLock lock = new ReentrantLock();

    /**
     * ��ȡ�����ѯ������count����
     *
     * @param select
     * @return
     */
    public static long count(ISelect select) {
        Page page = startPage(1, -1, true);
        select.doSelect();
        return page.getTotalCount();
    }

    /**
     * ��ʼ��ҳ
     *
     * @param pageNo  ҳ��
     * @param pageSize ÿҳ��ʾ����
     */
    public static Page startPage(long pageNo, long pageSize) {
        return startPage(pageNo, pageSize, true);
    }

    /**
     * ��ʼ��ҳ
     *
     * @param pageNo  ҳ��
     * @param pageSize ÿҳ��ʾ����
     * @param count    �Ƿ����count��ѯ
     */
    public static Page startPage(long pageNo, long pageSize, boolean count) {
        return startPage(pageNo, pageSize, count, null);
    }

    /**
     * ��ʼ��ҳ
     *
     * @param pageNo  ҳ��
     * @param pageSize ÿҳ��ʾ����
     * @param orderBy  ����
     */
    public static Page startPage(long pageNo, long pageSize, String orderBy) {
        Page page = startPage(pageNo, pageSize);
        page.setOrderBy(orderBy);
        return page;
    }

    /**
     * ��ʼ��ҳ
     *
     * @param offset ҳ��
     * @param limit  ÿҳ��ʾ����
     */
    public static Page offsetPage(int offset, int limit) {
        return offsetPage(offset, limit, true);
    }

    /**
     * ��ʼ��ҳ
     *
     * @param offset ҳ��
     * @param limit  ÿҳ��ʾ����
     * @param count  �Ƿ����count��ѯ
     */
    public static Page offsetPage(int offset, int limit, boolean count) {
        Page page = new Page(new int[]{offset, limit}, count);
        //���Ѿ�ִ�й�orderBy��ʱ��
        Page oldPage = SqlUtil.getLocalPage();
        if (oldPage != null && oldPage.isOrderByOnly()) {
            page.setOrderBy(oldPage.getOrderBy());
        }
        SqlUtil.setLocalPage(page);
        return page;
    }

    /**
     * ��ʼ��ҳ
     *
     * @param offset  ҳ��
     * @param limit   ÿҳ��ʾ����
     * @param orderBy ����
     */
    public static Page offsetPage(int offset, int limit, String orderBy) {
        Page page = offsetPage(offset, limit);
        page.setOrderBy(orderBy);
        return page;
    }

    /**
     * ��ʼ��ҳ
     *
     * @param pageNo    ҳ��
     * @param pageSize   ÿҳ��ʾ����
     * @param count      �Ƿ����count��ѯ
     * @param reasonable ��ҳ����,nullʱ��Ĭ������
     */
    public static Page startPage(long pageNo, long pageSize, boolean count, Boolean reasonable) {
        return startPage(pageNo, pageSize, count, reasonable, null);
    }

    /**
     * ��ʼ��ҳ
     *
     * @param pageNo      ҳ��
     * @param pageSize     ÿҳ��ʾ����
     * @param count        �Ƿ����count��ѯ
     * @param reasonable   ��ҳ����,nullʱ��Ĭ������
     * @param pageSizeZero true��pageSize=0ʱ����ȫ�������falseʱ��ҳ,nullʱ��Ĭ������
     */
    public static Page startPage(long pageNo, long pageSize, boolean count, Boolean reasonable, Boolean pageSizeZero) {
        Page page = new Page(pageNo, pageSize, count);
        page.setPageSizeZero(pageSizeZero);
        //���Ѿ�ִ�й�orderBy��ʱ��
        Page oldPage = SqlUtil.getLocalPage();
        if (oldPage != null && oldPage.isOrderByOnly()) {
            page.setOrderBy(oldPage.getOrderBy());
        }
        SqlUtil.setLocalPage(page);
        return page;
    }

    /**
     * ��ʼ��ҳ
     *
     * @param params
     */
    public static Page startPage(Object params) {
        Page page = SqlUtil.getPageFromObject(params);
        //���Ѿ�ִ�й�orderBy��ʱ��
        Page oldPage = SqlUtil.getLocalPage();
        if (oldPage != null && oldPage.isOrderByOnly()) {
            page.setOrderBy(oldPage.getOrderBy());
        }
        SqlUtil.setLocalPage(page);
        return page;
    }

    /**
     * ����
     *
     * @param orderBy
     */
    public static void orderBy(String orderBy) {
        Page page = SqlUtil.getLocalPage();
        if (page != null) {
            page.setOrderBy(orderBy);
        } else {
            page = new Page();
            page.setOrderBy(orderBy);
            page.setOrderByOnly(true);
            SqlUtil.setLocalPage(page);
        }
    }

    /**
     * ��ȡorderBy
     *
     * @return
     */
    public static String getOrderBy() {
        Page page = SqlUtil.getLocalPage();
        if (page != null) {
            String orderBy = page.getOrderBy();
            if (StringUtil.isEmpty(orderBy)) {
                return null;
            } else {
                return orderBy;
            }
        }
        return null;
    }

    /**
     * Mybatis����������
     *
     * @param invocation ���������
     * @return ����ִ�н��
     * @throws Throwable �׳��쳣
     */
    public Object intercept(Invocation invocation) throws Throwable {
        if (autoRuntimeDialect) {
            SqlUtil sqlUtil = getSqlUtil(invocation);
            return sqlUtil.processPage(invocation);
        } else {
            if (autoDialect) {
                initSqlUtil(invocation);
            }
            return sqlUtil.processPage(invocation);
        }
    }

    /**
     * ��ʼ��sqlUtil
     *
     * @param invocation
     */
    public synchronized void initSqlUtil(Invocation invocation) {
        if (this.sqlUtil == null) {
            this.sqlUtil = getSqlUtil(invocation);
            if (!autoRuntimeDialect) {
                properties = null;
                sqlUtilConfig = null;
            }
            autoDialect = false;
        }
    }

    /**
     * ��ȡurl
     *
     * @param dataSource
     * @return
     */
    public String getUrl(DataSource dataSource){
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            return conn.getMetaData().getURL();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if(conn != null){
                try {
                    if(closeConn){
                        conn.close();
                    }
                } catch (SQLException e) {
                    //ignore
                }
            }
        }
    }

    /**
     * ����datasource������Ӧ��sqlUtil
     *
     * @param invocation
     */
    public SqlUtil getSqlUtil(Invocation invocation) {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        //��Ϊ��dataSource������
        DataSource dataSource = ms.getConfiguration().getEnvironment().getDataSource();
        String url = getUrl(dataSource);
        if (urlSqlUtilMap.containsKey(url)) {
            return urlSqlUtilMap.get(url);
        }
        try {
            lock.lock();
            if (urlSqlUtilMap.containsKey(url)) {
                return urlSqlUtilMap.get(url);
            }
            if (StringUtil.isEmpty(url)) {
                throw new RuntimeException("�޷��Զ���ȡjdbcUrl�����ڷ�ҳ���������dialect����!");
            }
            String dialect = Dialect.fromJdbcUrl(url);
            if (dialect == null) {
                throw new RuntimeException("�޷��Զ���ȡ���ݿ����ͣ���ͨ��dialect����ָ��!");
            }
            SqlUtil sqlUtil = new SqlUtil(dialect);
            if (this.properties != null) {
                sqlUtil.setProperties(properties);
            } else if (this.sqlUtilConfig != null) {
                sqlUtil.setSqlUtilConfig(this.sqlUtilConfig);
            }
            urlSqlUtilMap.put(url, sqlUtil);
            return sqlUtil;
        } finally {
            lock.unlock();
        }
    }

    /**
     * ֻ����Executor
     *
     * @param target
     * @return
     */
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    private void checkVersion() {
        //MyBatis3.2.0�汾У��
        try {
            Class.forName("org.apache.ibatis.scripting.xmltags.SqlNode");//SqlNode��3.2.0֮����������
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("��ʹ�õ�MyBatis�汾̫�ͣ�MyBatis��ҳ���PageHelper֧��MyBatis3.2.0�����ϰ汾!");
        }
    }

    /**
     * ��������ֵ
     *
     * @param p ����ֵ
     */
    public void setProperties(Properties p) {
        checkVersion();
        //������Դʱ����ȡjdbcurl���Ƿ�ر�����Դ
        String closeConn = p.getProperty("closeConn");
        //���#97
        if(StringUtil.isNotEmpty(closeConn)){
            this.closeConn = Boolean.parseBoolean(closeConn);
        }
        //��ʼ��SqlUtil��PARAMS
        SqlUtil.setParams(p.getProperty("params"));
        //���ݿⷽ��
        String dialect = p.getProperty("dialect");
        String runtimeDialect = p.getProperty("autoRuntimeDialect");
        if (StringUtil.isNotEmpty(runtimeDialect) && runtimeDialect.equalsIgnoreCase("TRUE")) {
            this.autoRuntimeDialect = true;
            this.autoDialect = false;
            this.properties = p;
        } else if (StringUtil.isEmpty(dialect)) {
            autoDialect = true;
            this.properties = p;
        } else {
            autoDialect = false;
            sqlUtil = new SqlUtil(dialect);
            sqlUtil.setProperties(p);
        }
    }

    /**
     * ��������ֵ
     *
     * @param config
     */
    public void setSqlUtilConfig(SqlUtilConfig config) {
        checkVersion();
        //��ʼ��SqlUtil��PARAMS
        SqlUtil.setParams(config.getParams());
        //������Դʱ����ȡjdbcurl���Ƿ�ر�����Դ
        this.closeConn = config.isCloseConn();
        if (config.isAutoRuntimeDialect()) {
            this.autoRuntimeDialect = true;
            this.autoDialect = false;
            this.sqlUtilConfig = config;
        } else if (StringUtil.isEmpty(config.getDialect())) {
            autoDialect = true;
            this.sqlUtilConfig = config;
        } else {
            autoDialect = false;
            sqlUtil = new SqlUtil(config.getDialect());
            sqlUtil.setSqlUtilConfig(config);
        }
    }
}
