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

import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.session.RowBounds;

import com.appleframework.orm.mybatis.pagehelper.parser.Parser;
import com.appleframework.orm.mybatis.pagehelper.parser.impl.AbstractParser;
import com.appleframework.orm.mybatis.pagehelper.sqlsource.PageDynamicSqlSource;
import com.appleframework.orm.mybatis.pagehelper.sqlsource.PageProviderSqlSource;
import com.appleframework.orm.mybatis.pagehelper.sqlsource.PageRawSqlSource;
import com.appleframework.orm.mybatis.pagehelper.sqlsource.PageSqlSource;
import com.appleframework.orm.mybatis.pagehelper.sqlsource.PageStaticSqlSource;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mybatis - sql���ߣ���ȡ��ҳ��count��MappedStatement�����÷�ҳ����
 *
 * @author liuzh/abel533/isea533
 * @since 3.6.0
 * ��Ŀ��ַ : http://git.oschina.net/free/Mybatis_PageHelper
 */
@SuppressWarnings({"rawtypes"})
public class SqlUtil implements Constant {
    private static final ThreadLocal<Page> LOCAL_PAGE = new ThreadLocal<Page>();
    //params����ӳ��
    private static Map<String, String> PARAMS = new HashMap<String, String>(5);
    //request��ȡ����
    private static Boolean hasRequest;
    private static Class<?> requestClass;
    private static Method getParameterMap;

    static {
        try {
            requestClass = Class.forName("javax.servlet.ServletRequest");
            getParameterMap = requestClass.getMethod("getParameterMap", new Class[]{});
            hasRequest = true;
        } catch (Throwable e) {
            hasRequest = false;
        }
    }

    //����count��ѯ��ms
    private static final Map<String, MappedStatement> msCountMap = new ConcurrentHashMap<String, MappedStatement>();
    
    //RowBounds����offset��ΪPageNumʹ�� - Ĭ�ϲ�ʹ��
    private boolean offsetAsPageNo = false;
    
    //RowBounds�Ƿ����count��ѯ - Ĭ�ϲ���ѯ
    private boolean rowBoundsWithCount = false;
    
    //������Ϊtrue��ʱ�����pagesize����Ϊ0����RowBounds��limit=0�����Ͳ�ִ�з�ҳ������ȫ�����
    private boolean pageSizeZero = false;

    //����������ݿ��parser
    private Parser parser;
    //�Ƿ�֧�ֽӿڲ��������ݷ�ҳ������Ĭ��false
    private boolean supportMethodsArguments = false;
    /**
     * ���췽��
     *
     * @param strDialect
     */
    public SqlUtil(String strDialect) {
        if (strDialect == null || "".equals(strDialect)) {
            throw new IllegalArgumentException("Mybatis��ҳ����޷���ȡdialect����!");
        }
        Exception exception = null;
        try {
            Dialect dialect = Dialect.of(strDialect);
            parser = AbstractParser.newParser(dialect);
        } catch (Exception e) {
            exception = e;
            //�쳣��ʱ���Է��䣬�����Լ�дʵ���ഫ�ݽ���
            try {
                Class<?> parserClass = Class.forName(strDialect);
                if (Parser.class.isAssignableFrom(parserClass)) {
                    parser = (Parser) parserClass.newInstance();
                }
            } catch (ClassNotFoundException ex) {
                exception = ex;
            } catch (InstantiationException ex) {
                exception = ex;
            } catch (IllegalAccessException ex) {
                exception = ex;
            }
        }
        if (parser == null) {
            throw new RuntimeException(exception);
        }
    }

    public static Boolean getCOUNT() {
        Page page = getLocalPage();
        if (page != null) {
            return page.getCountSignal();
        }
        return null;
    }

    /**
     * ��ȡPage����
     *
     * @return
     */
    public static Page getLocalPage() {
        return LOCAL_PAGE.get();
    }

    public static void setLocalPage(Page page) {
        LOCAL_PAGE.set(page);
    }

    /**
     * �Ƴ����ر���
     */
    public static void clearLocalPage() {
        LOCAL_PAGE.remove();
    }

    /**
     * �����л�ȡ��ҳ����
     *
     * @param params
     * @return
     */
    public static Page getPageFromObject(Object params) {
        int pageNo;
        int pageSize;
        MetaObject paramsObject = null;
        if (params == null) {
            throw new NullPointerException("�޷���ȡ��ҳ��ѯ����!");
        }
        if (hasRequest && requestClass.isAssignableFrom(params.getClass())) {
            try {
                paramsObject = SystemMetaObject.forObject(getParameterMap.invoke(params, new Object[]{}));
            } catch (Exception e) {
                //����
            }
        } else {
            paramsObject = SystemMetaObject.forObject(params);
        }
        if (paramsObject == null) {
            throw new NullPointerException("��ҳ��ѯ��������ʧ��!");
        }
        Object orderBy = getParamValue(paramsObject, "orderBy", false);
        boolean hasOrderBy = false;
        if (orderBy != null && orderBy.toString().length() > 0) {
            hasOrderBy = true;
        }
        try {
            Object _pageNo = getParamValue(paramsObject, "pageNo", hasOrderBy ? false : true);
            Object _pageSize = getParamValue(paramsObject, "pageSize", hasOrderBy ? false : true);
            if (_pageNo == null || _pageSize == null) {
                Page page = new Page();
                page.setOrderBy(orderBy.toString());
                page.setOrderByOnly(true);
                return page;
            }
            pageNo = Integer.parseInt(String.valueOf(_pageNo));
            pageSize = Integer.parseInt(String.valueOf(_pageSize));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("��ҳ�������ǺϷ�����������!");
        }
        Page page = new Page(pageNo, pageSize);
        //count��ѯ
        Object _count = getParamValue(paramsObject, "count", false);
        if (_count != null) {
            page.setCount(Boolean.valueOf(String.valueOf(_count)));
        }
        //����
        if (hasOrderBy) {
            page.setOrderBy(orderBy.toString());
        }
        //��ѯȫ��
        Object pageSizeZero = getParamValue(paramsObject, "pageSizeZero", false);
        if (pageSizeZero != null) {
            page.setPageSizeZero(Boolean.valueOf(String.valueOf(pageSizeZero)));
        }
        return page;
    }

    /**
     * �Ӷ�����ȡ����
     *
     * @param paramsObject
     * @param paramName
     * @param required
     * @return
     */
    public static Object getParamValue(MetaObject paramsObject, String paramName, boolean required) {
        Object value = null;
        if (paramsObject.hasGetter(PARAMS.get(paramName))) {
            value = paramsObject.getValue(PARAMS.get(paramName));
        }
        if (value != null && value.getClass().isArray()) {
            Object[] values = (Object[]) value;
            if (values.length == 0) {
                value = null;
            } else {
                value = values[0];
            }
        }
        if (required && value == null) {
            throw new RuntimeException("��ҳ��ѯȱ�ٱ�Ҫ�Ĳ���:" + PARAMS.get(paramName));
        }
        return value;
    }

    /**
     * �Ƿ��Ѿ������
     *
     * @param ms
     * @return
     */
    public boolean isPageSqlSource(MappedStatement ms) {
        if (ms.getSqlSource() instanceof PageSqlSource) {
            return true;
        }
        return false;
    }

    /**
     * ����[����̨���]count�ͷ�ҳsql
     *
     * @param dialect     ���ݿ�����
     * @param originalSql ԭsql
     * @deprecated ����5.x�汾ȥ��
     */
    @Deprecated
    public static void testSql(String dialect, String originalSql) {
        testSql(Dialect.of(dialect), originalSql);
    }

    /**
     * ����[����̨���]count�ͷ�ҳsql
     *
     * @param dialect     ���ݿ�����
     * @param originalSql ԭsql
     * @deprecated ����5.x�汾ȥ��
     */
    @Deprecated
    public static void testSql(Dialect dialect, String originalSql) {
        Parser parser = AbstractParser.newParser(dialect);
        if (dialect == Dialect.sqlserver) {
            setLocalPage(new Page(1, 10));
        }
        String countSql = parser.getCountSql(originalSql);
        System.out.println(countSql);
        String pageSql = parser.getPageSql(originalSql);
        System.out.println(pageSql);
        if (dialect == Dialect.sqlserver) {
            clearLocalPage();
        }
    }

    /**
     * �޸�SqlSource
     *
     * @param ms
     * @throws Throwable
     */
    public void processMappedStatement(MappedStatement ms) throws Throwable {
        SqlSource sqlSource = ms.getSqlSource();
        MetaObject msObject = SystemMetaObject.forObject(ms);
        SqlSource pageSqlSource;
        if (sqlSource instanceof StaticSqlSource) {
            pageSqlSource = new PageStaticSqlSource((StaticSqlSource) sqlSource);
        } else if (sqlSource instanceof RawSqlSource) {
            pageSqlSource = new PageRawSqlSource((RawSqlSource) sqlSource);
        } else if (sqlSource instanceof ProviderSqlSource) {
            pageSqlSource = new PageProviderSqlSource((ProviderSqlSource) sqlSource);
        } else if (sqlSource instanceof DynamicSqlSource) {
            pageSqlSource = new PageDynamicSqlSource((DynamicSqlSource) sqlSource);
        } else {
            throw new RuntimeException("�޷����������[" + sqlSource.getClass() + "]��SqlSource");
        }
        msObject.setValue("sqlSource", pageSqlSource);
        //����count��ѯ��Ҫ�޸ķ���ֵ���������Ҫ����һ��Count��ѯ��MS
        msCountMap.put(ms.getId(), MSUtils.newCountMappedStatement(ms));
    }

    /**
     * ��ȡ��ҳ����
     *
     * @param args
     * @return ����Page����
     */
    public Page getPage(Object[] args) {
        Page page = getLocalPage();
        if (page == null || page.isOrderByOnly()) {
            Page oldPage = page;
            //���������,page.isOrderByOnly()��ȻΪtrue�����Բ���д��������
            if ((args[2] == null || args[2] == RowBounds.DEFAULT) && page != null) {
                return oldPage;
            }
            if (args[2] instanceof RowBounds && args[2] != RowBounds.DEFAULT) {
                RowBounds rowBounds = (RowBounds) args[2];
                if (offsetAsPageNo) {
                    page = new Page(rowBounds.getOffset(), rowBounds.getLimit(), rowBoundsWithCount);
                } else {
                    page = new Page(new int[]{rowBounds.getOffset(), rowBounds.getLimit()}, rowBoundsWithCount);
                    //offsetAsPageNo=false��ʱ������PageNum���⣬����ʹ��reasonable�������ǿ��Ϊfalse
                }
            } else {
                try {
                    page = getPageFromObject(args[1]);
                } catch (Exception e) {
                    return null;
                }
            }
            if (oldPage != null) {
                page.setOrderBy(oldPage.getOrderBy());
            }
            setLocalPage(page);
        }
        //������Ϊtrue��ʱ�����pagesize����Ϊ0����RowBounds��limit=0�����Ͳ�ִ�з�ҳ������ȫ�����
        if (page.getPageSizeZero() == null) {
            page.setPageSizeZero(pageSizeZero);
        }
        return page;
    }

    /**
     * Mybatis��������������һ��Ƕ��Ϊ���ڳ����쳣ʱҲ�������Threadlocal
     *
     * @param invocation ���������
     * @return ����ִ�н��
     * @throws Throwable �׳��쳣
     */
    public Object processPage(Invocation invocation) throws Throwable {
        try {
            Object result = _processPage(invocation);
            return result;
        } finally {
            clearLocalPage();
        }
    }

    /**
     * Mybatis����������
     *
     * @param invocation ���������
     * @return ����ִ�н��
     * @throws Throwable �׳��쳣
     */
    private Object _processPage(Invocation invocation) throws Throwable {
        final Object[] args = invocation.getArgs();
        Page page = null;
        //֧�ַ�������ʱ�����ȳ��Ի�ȡPage
        if (supportMethodsArguments) {
            page = getPage(args);
        }
        //��ҳ��Ϣ
        RowBounds rowBounds = (RowBounds) args[2];
        //֧�ַ�������ʱ�����page == null��˵��û�з�ҳ����������Ҫ��ҳ��ѯ
        if ((supportMethodsArguments && page == null)
                //����֧�ַ�ҳ����ʱ���ж�LocalPage��RowBounds�ж��Ƿ���Ҫ��ҳ
                || (!supportMethodsArguments && SqlUtil.getLocalPage() == null && rowBounds == RowBounds.DEFAULT)) {
            return invocation.proceed();
        } else {
            //��֧�ַ�ҳ����ʱ��page==null��������Ҫ��ȡ
            if (!supportMethodsArguments && page == null) {
                page = getPage(args);
            }
            return doProcessPage(invocation, page, args);
        }
    }

    /**
     * �Ƿ�ֻ����ѯ
     *
     * @param page
     * @return
     */
    private boolean isQueryOnly(Page page) {
        return page.isOrderByOnly()
                || ((page.getPageSizeZero() != null && page.getPageSizeZero()) && page.getPageSize() == 0);
    }

    /**
     * ֻ����ѯ
     *
     * @param page
     * @param invocation
     * @return
     * @throws Throwable
     */
    private Page doQueryOnly(Page page, Invocation invocation) throws Throwable {
        page.setCountSignal(null);
        //ִ������������ҳ����ѯ
        Object result = invocation.proceed();
        //�õ�������
        page.setList((List) result);
        //�൱�ڲ�ѯ��һҳ
        page.setPageNo(1);
        //��������൱��pageSize=total
        page.setPageSize(page.getPageSize());
        //��ȻҪ����total
        page.setTotalCount(page.getPageSize());
        //���ؽ����ȻΪPage���� - ���ں���Խ������͵�ͳһ����
        return page;
    }

    /**
     * Mybatis����������
     *
     * @param invocation ���������
     * @return ����ִ�н��
     * @throws Throwable �׳��쳣
     */
    private Page doProcessPage(Invocation invocation, Page page, Object[] args) throws Throwable {
        //����RowBounds״̬
        RowBounds rowBounds = (RowBounds) args[2];
        //��ȡԭʼ��ms
        MappedStatement ms = (MappedStatement) args[0];
        //�жϲ�����ΪPageSqlSource
        if (!isPageSqlSource(ms)) {
            processMappedStatement(ms);
        }
        //���õ�ǰ��parser������ÿ��ʹ��ǰ����set��ThreadLocal��ֵ�����������Ӱ��
        ((PageSqlSource)ms.getSqlSource()).setParser(parser);
        try {
            //����RowBounds-��������Mybatis�Դ����ڴ��ҳ
            args[2] = RowBounds.DEFAULT;
            //���ֻ�������� �� pageSizeZero���ж�
            if (isQueryOnly(page)) {
                return doQueryOnly(page, invocation);
            }

            //�򵥵�ͨ��total��ֵ���ж��Ƿ����count��ѯ
            if (page.isCount()) {
                page.setCountSignal(Boolean.TRUE);
                //�滻MS
                args[0] = msCountMap.get(ms.getId());
                //��ѯ����
                Object result = invocation.proceed();
                //��ԭms
                args[0] = ms;
                //��������
                page.setTotalCount((Integer) ((List) result).get(0));
                if (page.getTotalCount() == 0) {
                    return page;
                }
            } else {
                page.setTotalCount(-1l);
            }
            //pageSize>0��ʱ��ִ�з�ҳ��ѯ��pageSize<=0��ʱ��ִ���൱�ڿ���ֻ������һ��count
            if (page.getPageSize() > 0 &&
                    ((rowBounds == RowBounds.DEFAULT && page.getPageNo() > 0)
                            || rowBounds != RowBounds.DEFAULT)) {
                //�������е�MappedStatement�滻Ϊ�µ�qs
                page.setCountSignal(null);
                BoundSql boundSql = ms.getBoundSql(args[1]);
                args[1] = parser.setPageParameter(ms, args[1], boundSql, page);
                page.setCountSignal(Boolean.FALSE);
                //ִ�з�ҳ��ѯ
                Object result = invocation.proceed();
                //�õ�������
                page.setList((List) result);
            }
        } finally {
            ((PageSqlSource)ms.getSqlSource()).removeParser();
        }

        //���ؽ��
        return page;
    }

    public void setOffsetAsPageNo(boolean offsetAsPageNo) {
        this.offsetAsPageNo = offsetAsPageNo;
    }

    public void setRowBoundsWithCount(boolean rowBoundsWithCount) {
        this.rowBoundsWithCount = rowBoundsWithCount;
    }

    public void setPageSizeZero(boolean pageSizeZero) {
        this.pageSizeZero = pageSizeZero;
    }
    
    public void setSupportMethodsArguments(boolean supportMethodsArguments) {
        this.supportMethodsArguments = supportMethodsArguments;
    }

    public static void setParams(String params) {
        PARAMS.put("pageNo", "pageNo");
        PARAMS.put("pageSize", "pageSize");
        PARAMS.put("count", "countSql");
        PARAMS.put("orderBy", "orderBy");
        PARAMS.put("reasonable", "reasonable");
        PARAMS.put("pageSizeZero", "pageSizeZero");
        if (StringUtil.isNotEmpty(params)) {
            String[] ps = params.split("[;|,|&]");
            for (String s : ps) {
                String[] ss = s.split("[=|:]");
                if (ss.length == 2) {
                    PARAMS.put(ss[0], ss[1]);
                }
            }
        }
    }

    public void setProperties(Properties p) {
        //offset��ΪPageNumʹ��
        String offsetAsPageNo = p.getProperty("offsetAsPageNo");
        this.offsetAsPageNo = Boolean.parseBoolean(offsetAsPageNo);
        //RowBounds��ʽ�Ƿ���count��ѯ
        String rowBoundsWithCount = p.getProperty("rowBoundsWithCount");
        this.rowBoundsWithCount = Boolean.parseBoolean(rowBoundsWithCount);
        //������Ϊtrue��ʱ�����pagesize����Ϊ0����RowBounds��limit=0�����Ͳ�ִ�з�ҳ
        String pageSizeZero = p.getProperty("pageSizeZero");
        this.pageSizeZero = Boolean.parseBoolean(pageSizeZero);
        //��ҳ������true�����������ҳ������������Զ�������Ĭ��false������
        //�Ƿ�֧�ֽӿڲ��������ݷ�ҳ������Ĭ��false
        String supportMethodsArguments = p.getProperty("supportMethodsArguments");
        this.supportMethodsArguments = Boolean.parseBoolean(supportMethodsArguments);
        //��offsetAsPageNo=false��ʱ�򣬲���
        //����ӳ��
        setParams(p.getProperty("params"));
    }

    public void setSqlUtilConfig(SqlUtilConfig config) {
        this.offsetAsPageNo = config.isOffsetAsPageNo();
        this.rowBoundsWithCount = config.isRowBoundsWithCount();
        this.pageSizeZero = config.isPageSizeZero();
        this.supportMethodsArguments = config.isSupportMethodsArguments();
        setParams(config.getParams());
    }
}