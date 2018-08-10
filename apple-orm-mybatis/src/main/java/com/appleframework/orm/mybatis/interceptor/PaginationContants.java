package com.appleframework.orm.mybatis.interceptor;

import com.appleframework.model.page.SimplePage;


public class PaginationContants {
	
    private static final ThreadLocal<SimplePage> LOCAL_PAGE = new ThreadLocal<SimplePage>();

    /**
     * 获取Page参数
     *
     * @return
     */
    public static SimplePage getLocalPage() {
        return LOCAL_PAGE.get();
    }

    public static void setLocalPage(SimplePage page) {
        LOCAL_PAGE.set(page);
    }

    /**
     * 移除本地变量
     */
    public static void clearLocalPage() {
        LOCAL_PAGE.remove();
    }

}