package com.appleframework.orm.mybatis.parser;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.appleframework.model.page.Pagination;

/**
 * ��sqlserver��ѯ���ת��Ϊ��ҳ���<br>
 * ע�����<br>
 * <ol>
 * <li>���ȱ�֤���SQL����ִ��</li>
 * <li>sql�����ֱ�Ӱ���order by�������Զ���sql��ȡ</li>
 * <li>���û��order by������ͨ������ṩ��������Ҫ�Լ���֤��ȷ</li>
 * <li>���sql��order by������ͨ��orderby��������sql�е�order by</li>
 * <li>order by����������ʹ�ñ���</li>
 * <li>�����ʹ�ñ�����ʱ��Ҫʹ�õ�����(')</li>
 * </ol>
 * �������Ϊһ�������Ĺ����࣬����jsqlparser,���Զ���ʹ��
 *
 * @author liuzh
 */
public class SqlServer {
    //������
    private static final Map<String, String> CACHE = new ConcurrentHashMap<String, String>();
    //��ʼ�к�
    private static final String START_ROW = String.valueOf(Long.MIN_VALUE);
    //�����к�
    private static final String PAGE_SIZE = String.valueOf(Long.MAX_VALUE);
    //����װ��
    private static final String WRAP_TABLE = "WRAP_OUTER_TABLE";
    //���������
    private static final String PAGE_TABLE_NAME = "PAGE_TABLE_ALIAS";
    //private
    public static final Alias PAGE_TABLE_ALIAS = new Alias(PAGE_TABLE_NAME);
    //�к�
    private static final String PAGE_ROW_NUMBER = "PAGE_ROW_NUMBER";
    //�к���
    private static final Column PAGE_ROW_NUMBER_COLUMN = new Column(PAGE_ROW_NUMBER);
    //TOP 100 PERCENT
    private static final Top TOP100_PERCENT;

    //��̬��������
    static {
        TOP100_PERCENT = new Top();
        TOP100_PERCENT.setRowCount(100);
        TOP100_PERCENT.setPercentage(true);
    }

    /**
     * ת��Ϊ��ҳ���
     *
     * @param sql
     * @param offset
     * @param limit
     * @return
     */
    public static String convertToPageSql(String sql, long offset, long limit) {
        String pageSql = CACHE.get(sql);
        if (pageSql == null) {
            //����SQL
            Statement stmt;
            try {
                stmt = CCJSqlParserUtil.parse(sql);
            } catch (Throwable e) {
                throw new RuntimeException("��֧�ָ�SQLת��Ϊ��ҳ��ѯ!");
            }
            if (!(stmt instanceof Select)) {
                throw new RuntimeException("��ҳ��������Select��ѯ!");
            }
            //��ȡ��ҳ��ѯ��select
            Select pageSelect = getPageSelect((Select) stmt);
            pageSql = pageSelect.toString();
            CACHE.put(sql, pageSql);
        }
        pageSql = pageSql.replace(START_ROW, String.valueOf(offset));
        pageSql = pageSql.replace(PAGE_SIZE, String.valueOf(limit));
        return pageSql;
    }

    /**
     * ��ȡһ������װ��TOP��ѯ
     *
     * @param select
     * @return
     */
    private static Select getPageSelect(Select select) {
        SelectBody selectBody = select.getSelectBody();
        if (selectBody instanceof SetOperationList) {
            selectBody = wrapSetOperationList((SetOperationList) selectBody);
        }
        //�����selectBodyһ����PlainSelect
        if (((PlainSelect) selectBody).getTop() != null) {
            throw new RuntimeException("����ҳ������Ѿ�������Top��������ͨ����ҳ������з�ҳ��ѯ!");
        }
        //��ȡ��ѯ��
        List<SelectItem> selectItems = getSelectItems((PlainSelect) selectBody);
        //��һ���SQL����ROW_NUMBER()
        addRowNumber((PlainSelect) selectBody);
        //����������е�order by
        processSelectBody(selectBody, 0);

        //�½�һ��select
        Select newSelect = new Select();
        PlainSelect newSelectBody = new PlainSelect();
        //����top
        Top top = new Top();
        top.setRowCount(Long.MAX_VALUE);
        newSelectBody.setTop(top);
        //����order by
        List<OrderByElement> orderByElements = new ArrayList<OrderByElement>();
        OrderByElement orderByElement = new OrderByElement();
        orderByElement.setExpression(PAGE_ROW_NUMBER_COLUMN);
        orderByElements.add(orderByElement);
        newSelectBody.setOrderByElements(orderByElements);
        //����where
        GreaterThan greaterThan = new GreaterThan();
        greaterThan.setLeftExpression(PAGE_ROW_NUMBER_COLUMN);
        greaterThan.setRightExpression(new LongValue(Long.MIN_VALUE));
        newSelectBody.setWhere(greaterThan);
        //����selectItems
        newSelectBody.setSelectItems(selectItems);
        //����fromIterm
        SubSelect fromItem = new SubSelect();
        fromItem.setSelectBody(selectBody);
        fromItem.setAlias(PAGE_TABLE_ALIAS);
        newSelectBody.setFromItem(fromItem);

        newSelect.setSelectBody(newSelectBody);
        if (isNotEmptyList(select.getWithItemsList())) {
            newSelect.setWithItemsList(select.getWithItemsList());
        }
        return newSelect;
    }

    /**
     * ��װSetOperationList
     *
     * @param setOperationList
     * @return
     */
    private static SelectBody wrapSetOperationList(SetOperationList setOperationList) {
        //��ȡ���һ��plainSelect
        PlainSelect plainSelect = setOperationList.getPlainSelects().get(setOperationList.getPlainSelects().size() - 1);

        PlainSelect selectBody = new PlainSelect();
        List<SelectItem> selectItems = getSelectItems(plainSelect);
        selectBody.setSelectItems(selectItems);

        //����fromIterm
        SubSelect fromItem = new SubSelect();
        fromItem.setSelectBody(setOperationList);
        fromItem.setAlias(new Alias(WRAP_TABLE));
        selectBody.setFromItem(fromItem);
        //order by
        if (isNotEmptyList(plainSelect.getOrderByElements())) {
            selectBody.setOrderByElements(plainSelect.getOrderByElements());
            plainSelect.setOrderByElements(null);
        }
        return selectBody;
    }

    /**
     * ��ȡ��ѯ��
     *
     * @param plainSelect
     * @return
     */
    private static List<SelectItem> getSelectItems(PlainSelect plainSelect) {
        //����selectItems
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        for (SelectItem selectItem : plainSelect.getSelectItems()) {
            //������Ҫ���⴦��
            if (selectItem instanceof SelectExpressionItem) {
                SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
                if (selectExpressionItem.getAlias() != null) {
                    //ֱ��ʹ�ñ���
                    Column column = new Column(selectExpressionItem.getAlias().getName());
                    SelectExpressionItem expressionItem = new SelectExpressionItem(column);
                    selectItems.add(expressionItem);
                } else if (selectExpressionItem.getExpression() instanceof Column) {
                    Column column = (Column) selectExpressionItem.getExpression();
                    SelectExpressionItem item = null;
                    if (column.getTable() != null) {
                        Column newColumn = new Column(column.getColumnName());
                        item = new SelectExpressionItem(newColumn);
                        selectItems.add(item);
                    } else {
                        selectItems.add(selectItem);
                    }
                } else {
                    selectItems.add(selectItem);
                }
            } else if (selectItem instanceof AllTableColumns) {
                selectItems.add(new AllColumns());
            } else {
                selectItems.add(selectItem);
            }
        }
        return selectItems;
    }

    /**
     * ������SQL��ѯ��Ҫ����ROW_NUMBER()
     *
     * @param plainSelect
     */
    private static void addRowNumber(PlainSelect plainSelect) {
        //����ROW_NUMBER()
        StringBuilder orderByBuilder = new StringBuilder();
        orderByBuilder.append("ROW_NUMBER() OVER (");
        if (isNotEmptyList(plainSelect.getOrderByElements())) {
            //ע�⣺order by������ʱ���д�,����û���ж�һ�����Ƿ�Ϊ���������Բ��ܽ��
            orderByBuilder.append(PlainSelect.orderByToString(false, plainSelect.getOrderByElements()));
        } else {
            throw new RuntimeException("������sql�а���order by���!");
        }
        //��Ҫ�Ѹ�orderby���
        if (isNotEmptyList(plainSelect.getOrderByElements())) {
            plainSelect.setOrderByElements(null);
        }
        orderByBuilder.append(") ");
        orderByBuilder.append(PAGE_ROW_NUMBER);
        Column orderByColumn = new Column(orderByBuilder.toString());
        plainSelect.getSelectItems().add(0, new SelectExpressionItem(orderByColumn));
    }

    /**
     * ����selectBodyȥ��Order by
     *
     * @param selectBody
     */
    private static void processSelectBody(SelectBody selectBody, int level) {
        if (selectBody instanceof PlainSelect) {
            processPlainSelect((PlainSelect) selectBody, level + 1);
        } else if (selectBody instanceof WithItem) {
            WithItem withItem = (WithItem) selectBody;
            if (withItem.getSelectBody() != null) {
                processSelectBody(withItem.getSelectBody(), level + 1);
            }
        } else {
            SetOperationList operationList = (SetOperationList) selectBody;
            if (operationList.getPlainSelects() != null && operationList.getPlainSelects().size() > 0) {
                List<PlainSelect> plainSelects = operationList.getPlainSelects();
                for (PlainSelect plainSelect : plainSelects) {
                    processPlainSelect(plainSelect, level + 1);
                }
            }
        }
    }

    /**
     * ����PlainSelect���͵�selectBody
     *
     * @param plainSelect
     */
    private static void processPlainSelect(PlainSelect plainSelect, int level) {
        if (level > 1) {
            if (isNotEmptyList(plainSelect.getOrderByElements())) {
                if (plainSelect.getTop() == null) {
                    plainSelect.setTop(TOP100_PERCENT);
                }
            }
        }
        if (plainSelect.getFromItem() != null) {
            processFromItem(plainSelect.getFromItem(), level + 1);
        }
        if (plainSelect.getJoins() != null && plainSelect.getJoins().size() > 0) {
            List<Join> joins = plainSelect.getJoins();
            for (Join join : joins) {
                if (join.getRightItem() != null) {
                    processFromItem(join.getRightItem(), level + 1);
                }
            }
        }
    }

    /**
     * �����Ӳ�ѯ
     *
     * @param fromItem
     */
    private static void processFromItem(FromItem fromItem, int level) {
        if (fromItem instanceof SubJoin) {
            SubJoin subJoin = (SubJoin) fromItem;
            if (subJoin.getJoin() != null) {
                if (subJoin.getJoin().getRightItem() != null) {
                    processFromItem(subJoin.getJoin().getRightItem(), level + 1);
                }
            }
            if (subJoin.getLeft() != null) {
                processFromItem(subJoin.getLeft(), level + 1);
            }
        } else if (fromItem instanceof SubSelect) {
            SubSelect subSelect = (SubSelect) fromItem;
            if (subSelect.getSelectBody() != null) {
                processSelectBody(subSelect.getSelectBody(), level + 1);
            }
        } else if (fromItem instanceof ValuesList) {

        } else if (fromItem instanceof LateralSubSelect) {
            LateralSubSelect lateralSubSelect = (LateralSubSelect) fromItem;
            if (lateralSubSelect.getSubSelect() != null) {
                SubSelect subSelect = lateralSubSelect.getSubSelect();
                if (subSelect.getSelectBody() != null) {
                    processSelectBody(subSelect.getSelectBody(), level + 1);
                }
            }
        }
        //Tableʱ���ô���
    }

    /**
     * List����
     *
     * @param list
     * @return
     */
    private static boolean isNotEmptyList(List<?> list) {
        if (list == null || list.size() == 0) {
            return false;
        }
        return true;
    }

    /**
     * �ַ�������
     *
     * @param str
     * @return
     */
    /*private boolean isNotEmptyString(String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        return true;
    }*/
    
    public static void main(String[] args) {
		String sql = "select * from c_contacts where is_delete=0";
		//SqlServer s = new SqlServer();
		Pagination page = new Pagination(3, 100);
		System.out.println( SqlServer.convertToPageSql(sql, (page.getPageNo() - 1) * page.getPageSize(), page.getPageSize()) );
	}
}

