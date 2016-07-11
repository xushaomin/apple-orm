/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 abel533@gmail.com
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

/**
 * ������Spring Boot
 *
 * @author liuzh_3nofxnp
 * @since 4.1.0
 */
public class SqlUtilConfig {
    //����
    private String dialect;
    //RowBounds����offset��ΪPageNumʹ�� - Ĭ�ϲ�ʹ��
    private boolean offsetAsPageNo = false;
    //RowBounds�Ƿ����count��ѯ - Ĭ�ϲ���ѯ
    private boolean rowBoundsWithCount = false;
    //������Ϊtrue��ʱ�����pagesize����Ϊ0����RowBounds��limit=0�����Ͳ�ִ�з�ҳ������ȫ�����
    private boolean pageSizeZero = false;
    //��ҳ����
    private boolean reasonable = false;
    //�Ƿ�֧�ֽӿڲ��������ݷ�ҳ������Ĭ��false
    private boolean supportMethodsArguments = false;
    //��������
    private String params;
    //����ʱ�Զ���ȡdialect
    private boolean autoRuntimeDialect;
    //������Դʱ����ȡjdbcurl���Ƿ�ر�����Դ
    private boolean closeConn = true;

    public String getDialect() {
        return dialect;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }

    public boolean isOffsetAsPageNo() {
        return offsetAsPageNo;
    }

    public void setOffsetAsPageNo(boolean offsetAsPageNo) {
        this.offsetAsPageNo = offsetAsPageNo;
    }

    public boolean isRowBoundsWithCount() {
        return rowBoundsWithCount;
    }

    public void setRowBoundsWithCount(boolean rowBoundsWithCount) {
        this.rowBoundsWithCount = rowBoundsWithCount;
    }

    public boolean isPageSizeZero() {
        return pageSizeZero;
    }

    public void setPageSizeZero(boolean pageSizeZero) {
        this.pageSizeZero = pageSizeZero;
    }

    public boolean isReasonable() {
        return reasonable;
    }

    public void setReasonable(boolean reasonable) {
        this.reasonable = reasonable;
    }

    public boolean isSupportMethodsArguments() {
        return supportMethodsArguments;
    }

    public void setSupportMethodsArguments(boolean supportMethodsArguments) {
        this.supportMethodsArguments = supportMethodsArguments;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public boolean isAutoRuntimeDialect() {
        return autoRuntimeDialect;
    }

    public void setAutoRuntimeDialect(boolean autoRuntimeDialect) {
        this.autoRuntimeDialect = autoRuntimeDialect;
    }

    public boolean isCloseConn() {
        return closeConn;
    }

    public void setCloseConn(boolean closeConn) {
        this.closeConn = closeConn;
    }
}
