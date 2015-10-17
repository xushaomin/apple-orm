package com.appleframework.orm.mybatis.parser;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * sql�����࣬�ṩ�����ܵ�count��ѯsql
 *
 * @author liuzh
 */
public class SqlParser {
	
    private static final List<SelectItem> COUNT_ITEM;
    private static final Alias TABLE_ALIAS;

    static {
        COUNT_ITEM = new ArrayList<SelectItem>();
        COUNT_ITEM.add(new SelectExpressionItem(new Column("count(*)")));

        TABLE_ALIAS = new Alias("table_count");
        TABLE_ALIAS.setUseAs(false);
    }

    //�����Ѿ��޸Ĺ���sql
    private static Map<String, String> CACHE = new ConcurrentHashMap<String, String>();

    public static void isSupportedSql(String sql) {
        if (sql.trim().toUpperCase().endsWith("FOR UPDATE")) {
            throw new RuntimeException("��ҳ�����֧�ְ���for update��sql");
        }
    }

    /**
     * ��ȡ���ܵ�countSql
     *
     * @param sql
     * @return
     */
    public static String getSmartCountSql(String sql) {
        //У���Ƿ�֧�ָ�sql
        isSupportedSql(sql);
        if (CACHE.get(sql) != null) {
            return CACHE.get(sql);
        }
        //����SQL
        Statement stmt = null;
        try {
            stmt = CCJSqlParserUtil.parse(sql);
        } catch (Throwable e) {
            //�޷���������һ�㷽������count���
            String countSql = getSimpleCountSql(sql);
            CACHE.put(sql, countSql);
            return countSql;
        }
        Select select = (Select) stmt;
        SelectBody selectBody = select.getSelectBody();
        //����body-ȥorder by
        processSelectBody(selectBody);
        //����with-ȥorder by
        processWithItemsList(select.getWithItemsList());
        //����Ϊcount��ѯ
        sqlToCount(select);
        String result = select.toString();
        CACHE.put(sql, result);
        return result;
    }

    /**
     * ��ȡ��ͨ��Count-sql
     *
     * @param sql ԭ��ѯsql
     * @return ����count��ѯsql
     */
    public static String getSimpleCountSql(final String sql) {
        isSupportedSql(sql);
        StringBuilder stringBuilder = new StringBuilder(sql.length() + 40);
        stringBuilder.append("select count(*) from (");
        stringBuilder.append(sql);
        stringBuilder.append(") tmp_count");
        return stringBuilder.toString();
    }

    /**
     * ��sqlת��Ϊcount��ѯ
     *
     * @param select
     */
    public static void sqlToCount(Select select) {
        SelectBody selectBody = select.getSelectBody();
        // �Ƿ��ܼ�count��ѯ
        if (selectBody instanceof PlainSelect && isSimpleCount((PlainSelect) selectBody)) {
            ((PlainSelect) selectBody).setSelectItems(COUNT_ITEM);
        } else {
            PlainSelect plainSelect = new PlainSelect();
            SubSelect subSelect = new SubSelect();
            subSelect.setSelectBody(selectBody);
            subSelect.setAlias(TABLE_ALIAS);
            plainSelect.setFromItem(subSelect);
            plainSelect.setSelectItems(COUNT_ITEM);
            select.setSelectBody(plainSelect);
        }
    }

    /**
     * �Ƿ�����ü򵥵�count��ѯ��ʽ
     *
     * @param select
     * @return
     */
    public static boolean isSimpleCount(PlainSelect select) {
        //����group by��ʱ�򲻿���
        if (select.getGroupByColumnReferences() != null) {
            return false;
        }
        //����distinct��ʱ�򲻿���
        if (select.getDistinct() != null) {
            return false;
        }
        for (SelectItem item : select.getSelectItems()) {
            //select���а���������ʱ�򲻿��ԣ���������������������
            if (item.toString().contains("?")) {
                return false;
            }
            //�����ѯ���а���������Ҳ�����ԣ��������ܻ�ۺ���
            if (item instanceof SelectExpressionItem) {
                if (((SelectExpressionItem) item).getExpression() instanceof Function) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * ����selectBodyȥ��Order by
     *
     * @param selectBody
     */
    public static void processSelectBody(SelectBody selectBody) {
        if (selectBody instanceof PlainSelect) {
            processPlainSelect((PlainSelect) selectBody);
        } else if (selectBody instanceof WithItem) {
            WithItem withItem = (WithItem) selectBody;
            if (withItem.getSelectBody() != null) {
                processSelectBody(withItem.getSelectBody());
            }
        } else {
            SetOperationList operationList = (SetOperationList) selectBody;
            if (operationList.getPlainSelects() != null && operationList.getPlainSelects().size() > 0) {
                List<PlainSelect> plainSelects = operationList.getPlainSelects();
                for (PlainSelect plainSelect : plainSelects) {
                    processPlainSelect(plainSelect);
                }
            }
            if (!orderByHashParameters(operationList.getOrderByElements())) {
                operationList.setOrderByElements(null);
            }
        }
    }

    /**
     * ����PlainSelect���͵�selectBody
     *
     * @param plainSelect
     */
    public static void processPlainSelect(PlainSelect plainSelect) {
        if (!orderByHashParameters(plainSelect.getOrderByElements())) {
            plainSelect.setOrderByElements(null);
        }
        if (plainSelect.getFromItem() != null) {
            processFromItem(plainSelect.getFromItem());
        }
        if (plainSelect.getJoins() != null && plainSelect.getJoins().size() > 0) {
            List<Join> joins = plainSelect.getJoins();
            for (Join join : joins) {
                if (join.getRightItem() != null) {
                    processFromItem(join.getRightItem());
                }
            }
        }
    }

    /**
     * ����WithItem
     *
     * @param withItemsList
     */
    public static void processWithItemsList(List<WithItem> withItemsList) {
        if (withItemsList != null && withItemsList.size() > 0) {
            for (WithItem item : withItemsList) {
                processSelectBody(item.getSelectBody());
            }
        }
    }

    /**
     * �����Ӳ�ѯ
     *
     * @param fromItem
     */
    public static void processFromItem(FromItem fromItem) {
        if (fromItem instanceof SubJoin) {
            SubJoin subJoin = (SubJoin) fromItem;
            if (subJoin.getJoin() != null) {
                if (subJoin.getJoin().getRightItem() != null) {
                    processFromItem(subJoin.getJoin().getRightItem());
                }
            }
            if (subJoin.getLeft() != null) {
                processFromItem(subJoin.getLeft());
            }
        } else if (fromItem instanceof SubSelect) {
            SubSelect subSelect = (SubSelect) fromItem;
            if (subSelect.getSelectBody() != null) {
                processSelectBody(subSelect.getSelectBody());
            }
        } else if (fromItem instanceof ValuesList) {

        } else if (fromItem instanceof LateralSubSelect) {
            LateralSubSelect lateralSubSelect = (LateralSubSelect) fromItem;
            if (lateralSubSelect.getSubSelect() != null) {
                SubSelect subSelect = lateralSubSelect.getSubSelect();
                if (subSelect.getSelectBody() != null) {
                    processSelectBody(subSelect.getSelectBody());
                }
            }
        }
        //Tableʱ���ô���
    }

    /**
     * �ж�Orderby�Ƿ�����������в����Ĳ���ȥ
     *
     * @param orderByElements
     * @return
     */
    public static boolean orderByHashParameters(List<OrderByElement> orderByElements) {
        if (orderByElements == null) {
            return false;
        }
        for (OrderByElement orderByElement : orderByElements) {
            if (orderByElement.toString().contains("?")) {
                return true;
            }
        }
        return false;
    }
    
    public static void main(String[] args) {
		String sql = "select * from c_contacts where is_delete=0 order by company_id asc ";
		System.out.println( SqlParser.getSimpleCountSql(sql) );
		System.out.println( SqlParser.getSmartCountSql(sql) );
	}
}

