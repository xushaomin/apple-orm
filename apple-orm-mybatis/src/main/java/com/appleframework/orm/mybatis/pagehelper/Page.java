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

import java.util.List;

import com.appleframework.model.page.Pagination;

/**
 * Mybatis - ��ҳ����
 *
 * @author liuzh/abel533/isea533
 * @version 3.6.0
 *          ��Ŀ��ַ : http://git.oschina.net/free/Mybatis_PageHelper
 */
public class Page extends Pagination {
	
    private static final long serialVersionUID = 1L;

    /**
     * ��ʼ��
     */
    private long startRow;
    /**
     * ĩ��
     */
    private long endRow;

    /**
     * ����count��ѯ
     */
    private boolean count;
    
    /**
     * count�źţ�3�������null��ʱ��ִ��Ĭ��BoundSql,true��ʱ��ִ��count��falseִ�з�ҳ
     */
    private Boolean countSignal;
    
    /**
     * ����
     */
    private String orderBy;
    
    /**
     * ֻ��������
     */
    private boolean orderByOnly;
        
    /**
     * ������Ϊtrue��ʱ�����pagesize����Ϊ0����RowBounds��limit=0�����Ͳ�ִ�з�ҳ������ȫ�����
     */
    private Boolean pageSizeZero;

    public Page() {
        super();
    }

    public Page(long pageNo, long pageSize) {
        this(pageNo, pageSize, true, null);
    }

    public Page(long pageNo, long pageSize, boolean count) {
        this(pageNo, pageSize, count, null);
    }

    private Page(long pageNo, long pageSize, boolean count, Boolean reasonable) {
        super(pageNo, pageSize);
        if (pageNo == 1 && pageSize == Integer.MAX_VALUE) {
            pageSizeZero = true;
            pageSize = 0;
        }
        super.pageNo = pageNo;
        super.pageSize = pageSize;
        this.count = count;
        calculateStartAndEndRow();
    }

    /**
     * int[] rowBounds
     * 0 : offset
     * 1 : limit
     */
    public Page(int[] rowBounds, boolean count) {
        super();
        if (rowBounds[0] == 0 && rowBounds[1] == Integer.MAX_VALUE) {
            pageSizeZero = true;
            this.pageSize = 0;
        } else {
            this.pageSize = rowBounds[1];
            this.pageNo = rowBounds[1] != 0 ? (int) (Math.ceil(((double) rowBounds[0] + rowBounds[1]) / rowBounds[1])) : 0;
        }
        this.startRow = rowBounds[0];
        this.count = count;
        this.endRow = this.startRow + rowBounds[1];
    }

    public List<?> getResult() {
        return super.getList();
    }

    public long getEndRow() {
        return endRow;
    }

    public Page setEndRow(int endRow) {
        this.endRow = endRow;
        return this;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public long getStartRow() {
        return startRow;
    }

    public Page setStartRow(int startRow) {
        this.startRow = startRow;
        return this;
    }

    public Boolean getPageSizeZero() {
        return pageSizeZero;
    }

    public Page setPageSizeZero(Boolean pageSizeZero) {
        if (pageSizeZero != null) {
            this.pageSizeZero = pageSizeZero;
        }
        return this;
    }

    /**
     * ������ֹ�к�
     */
    private void calculateStartAndEndRow() {
        this.startRow = this.pageNo > 0 ? (this.pageNo - 1) * this.pageSize : 0;
        this.endRow = this.startRow + this.pageSize * (this.pageNo > 0 ? 1 : 0);
    }

    public boolean isCount() {
        return this.count;
    }

    public Page setCount(boolean count) {
        this.count = count;
        return this;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public Page setOrderBy(String orderBy) {
        this.orderBy = orderBy;
        return (Page) this;
    }

    public boolean isOrderByOnly() {
        return orderByOnly;
    }

    public void setOrderByOnly(boolean orderByOnly) {
        this.orderByOnly = orderByOnly;
    }

    public Boolean getCountSignal() {
        return countSignal;
    }

    public void setCountSignal(Boolean countSignal) {
        this.countSignal = countSignal;
    }

    /**
     * �Ƿ�ִ��count��ѯ
     *
     * @param count
     * @return
     */
    public Page count(Boolean count) {
        this.count = count;
        return this;
    }

    /**
     * ������Ϊtrue��ʱ�����pagesize����Ϊ0����RowBounds��limit=0�����Ͳ�ִ�з�ҳ������ȫ�����
     *
     * @param pageSizeZero
     * @return
     */
    public Page pageSizeZero(Boolean pageSizeZero) {
        setPageSizeZero(pageSizeZero);
        return this;
    }

    public long doCount(ISelect select) {
        this.pageSizeZero = true;
        this.pageSize = 0;
        select.doSelect();
        return this.totalCount;
    }

	@Override
	public String toString() {
		return "Page [startRow=" + startRow + ", endRow=" + endRow + ", count=" + count + ", countSignal=" + countSignal
				+ ", orderBy=" + orderBy + ", orderByOnly=" + orderByOnly + ", pageSizeZero=" + pageSizeZero
				+ ", totalCount=" + totalCount + ", pageSize=" + pageSize + ", pageNo=" + pageNo + "]";
	}
	
}
