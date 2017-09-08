package com.appleframework.orm.mybatis.interceptor;

import com.appleframework.model.page.Pagination;

public class PaginationHelper {

	/**
	 * 开始分页
	 *
	 * @param pageNo
	 *            页码
	 * @param pageSize
	 *            每页显示数量
	 */
	public static Pagination startPage(long pageNo, long pageSize) {
		Pagination page = new Pagination(pageNo, pageSize);
		PaginationContants.setLocalPage(page);
		return page;
	}
	
	/**
	 * 获取分页
	 *
	 */
	public static Pagination getPage() {
		return PaginationContants.getLocalPage();
	}
	
	/**
	 * 清除分页
	 *
	 */
	public static void clearPage() {
		PaginationContants.clearLocalPage();
	}

}
