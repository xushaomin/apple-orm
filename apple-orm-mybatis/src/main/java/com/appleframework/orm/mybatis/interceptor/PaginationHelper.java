package com.appleframework.orm.mybatis.interceptor;

import com.appleframework.model.page.SimplePage;

public class PaginationHelper {

	/**
	 * 开始分页
	 *
	 * @param pageNo
	 *            页码
	 * @param pageSize
	 *            每页显示数量
	 */
	public static SimplePage startPage(long pageNo, long pageSize) {
		SimplePage page = new SimplePage(pageNo, pageSize);
		PaginationContants.setLocalPage(page);
		return page;
	}
	
	/**
	 * 开始分页
	 *
	 * @param page
	 *            分页对象
	 */
	public static SimplePage startPage(SimplePage page) {
		PaginationContants.setLocalPage(page);
		return page;
	}
	
	/**
	 * 获取分页
	 *
	 */
	public static SimplePage getPage() {
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
