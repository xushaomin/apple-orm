package com.appleframework.orm.mybatis.interceptor;

import com.appleframework.model.page.Pagination;


public class PaginationContants {
	
    private static final ThreadLocal<Pagination> LOCAL_PAGE = new ThreadLocal<Pagination>();

    /**
     * 获取Page参数
     *
     * @return
     */
    public static Pagination getLocalPage() {
        return LOCAL_PAGE.get();
    }

    public static void setLocalPage(Pagination page) {
        LOCAL_PAGE.set(page);
    }

    /**
     * 移除本地变量
     */
    public static void clearLocalPage() {
        LOCAL_PAGE.remove();
    }

}