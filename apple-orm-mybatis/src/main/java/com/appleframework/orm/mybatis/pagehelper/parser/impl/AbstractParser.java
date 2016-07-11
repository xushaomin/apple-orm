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

package com.appleframework.orm.mybatis.pagehelper.parser.impl;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;

import com.appleframework.orm.mybatis.pagehelper.Constant;
import com.appleframework.orm.mybatis.pagehelper.Dialect;
import com.appleframework.orm.mybatis.pagehelper.Page;
import com.appleframework.orm.mybatis.pagehelper.parser.Parser;
import com.appleframework.orm.mybatis.pagehelper.parser.SqlParser;
import com.appleframework.orm.mybatis.pagehelper.sqlsource.PageProviderSqlSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liuzh
 */
public abstract class AbstractParser implements Parser, Constant {
    //����SQL
    public static final SqlParser sqlParser = new SqlParser();

    public static Parser newParser(Dialect dialect) {
        Parser parser = null;
        switch (dialect) {
            case mysql:
            case mariadb:
            case sqlite:
                parser = new MysqlParser();
                break;
            case oracle:
                parser = new OracleParser();
                break;
            case hsqldb:
                parser = new HsqldbParser();
                break;
            case sqlserver:
                parser = new SqlServerParser();
                break;
            case sqlserver2012:
                parser = new SqlServer2012Dialect();
                break;
            case db2:
                parser = new Db2Parser();
                break;
            case postgresql:
                parser = new PostgreSQLParser();
                break;
            case informix:
                parser = new InformixParser();
                break;
            case h2:
                parser = new H2Parser();
                break;
            default:
                throw new RuntimeException("��ҳ���" + dialect + "���Դ���!");
        }
        return parser;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Map<String, Object> processParameter(MappedStatement ms, Object parameterObject, BoundSql boundSql) {
        Map<String, Object> paramMap = null;
        if (parameterObject == null) {
            paramMap = new HashMap<String, Object>();
        } else if (parameterObject instanceof Map) {
            //������ɱ�Map�����
            paramMap = new HashMap<String, Object>();
            paramMap.putAll((Map) parameterObject);
        } else {
            paramMap = new HashMap<String, Object>();
            //��̬sqlʱ���ж��������������ParameterMapping�У����Ǳ����У�����������Ҫ�ռ����е�getter����
            //TypeHandlerRegistry����ֱ�Ӵ���Ļ���Ϊһ��ֱ��ʹ�õĶ�����д���
            boolean hasTypeHandler = ms.getConfiguration().getTypeHandlerRegistry().hasTypeHandler(parameterObject.getClass());
            MetaObject metaObject = SystemMetaObject.forObject(parameterObject);
            //��Ҫ���ע����ʽ��MyProviderSqlSource����ԭֵ
            if (ms.getSqlSource() instanceof PageProviderSqlSource) {
                paramMap.put(PROVIDER_OBJECT, parameterObject);
            }
            if (!hasTypeHandler) {
                for (String name : metaObject.getGetterNames()) {
                    paramMap.put(name, metaObject.getValue(name));
                }
            }
            //������η�������Ҫ���һ���������͵Ĳ���ʱ������
            if (boundSql.getParameterMappings() != null && boundSql.getParameterMappings().size() > 0) {
                for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
                    String name = parameterMapping.getProperty();
                    if (!name.equals(PAGEPARAMETER_FIRST)
                            && !name.equals(PAGEPARAMETER_SECOND)
                            && paramMap.get(name) == null) {
                        if (hasTypeHandler
                                || parameterMapping.getJavaType().equals(parameterObject.getClass())) {
                            paramMap.put(name, parameterObject);
                            break;
                        }
                    }
                }
            }
        }
        //����ԭʼ��������
        paramMap.put(ORIGINAL_PARAMETER_OBJECT, parameterObject);
        return paramMap;
    }

    @Override
    public boolean isSupportedMappedStatementCache() {
        return true;
    }

    public String getCountSql(final String sql) {
        return sqlParser.getSmartCountSql(sql);
    }

    public abstract String getPageSql(String sql);

    public List<ParameterMapping> getPageParameterMapping(Configuration configuration, BoundSql boundSql) {
        List<ParameterMapping> newParameterMappings = new ArrayList<ParameterMapping>();
        if (boundSql != null && boundSql.getParameterMappings() != null) {
            newParameterMappings.addAll(boundSql.getParameterMappings());
        }
        newParameterMappings.add(new ParameterMapping.Builder(configuration, PAGEPARAMETER_FIRST, Integer.class).build());
        newParameterMappings.add(new ParameterMapping.Builder(configuration, PAGEPARAMETER_SECOND, Integer.class).build());
        return newParameterMappings;
    }

    public Map<String, Object> setPageParameter(MappedStatement ms, Object parameterObject, BoundSql boundSql, Page page) {
        return processParameter(ms, parameterObject, boundSql);
    }
}
