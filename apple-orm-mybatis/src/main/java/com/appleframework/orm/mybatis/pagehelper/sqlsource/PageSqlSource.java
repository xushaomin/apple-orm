package com.appleframework.orm.mybatis.pagehelper.sqlsource;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;

import com.appleframework.orm.mybatis.pagehelper.SqlUtil;
import com.appleframework.orm.mybatis.pagehelper.parser.Parser;

/**
 * ������Ϣ
 *
 * @author liuzh
 * @since 2015-06-29
 */
public abstract class PageSqlSource implements SqlSource {

    protected static final ThreadLocal<Parser> localParser = new ThreadLocal<Parser>();

    public void setParser(Parser parser) {
        localParser.set(parser);
    }

    public void removeParser(){
        localParser.remove();
    }

    /**
     * ����ֵnull - ��ͨ,true - count,false - page
     *
     * @return
     */
    protected Boolean getCount() {
        return SqlUtil.getCOUNT();
    }

    /**
     * ��ȡ������BoundSql
     *
     * @param parameterObject
     * @return
     */
    protected abstract BoundSql getDefaultBoundSql(Object parameterObject);

    /**
     * ��ȡCount��ѯ��BoundSql
     *
     * @param parameterObject
     * @return
     */
    protected abstract BoundSql getCountBoundSql(Object parameterObject);

    /**
     * ��ȡ��ҳ��ѯ��BoundSql
     *
     * @param parameterObject
     * @return
     */
    protected abstract BoundSql getPageBoundSql(Object parameterObject);

    /**
     * ��ȡBoundSql
     *
     * @param parameterObject
     * @return
     */
    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        Boolean count = getCount();
        if (count == null) {
            return getDefaultBoundSql(parameterObject);
        } else if (count) {
            return getCountBoundSql(parameterObject);
        } else {
            return getPageBoundSql(parameterObject);
        }
    }
}
