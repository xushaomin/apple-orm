package com.appleframework.orm.mybatis.page;

import java.util.HashMap;
import java.util.Map;

import com.appleframework.model.page.SimplePage;

/**
 * 封装查询蚕食和查询条件
 * 
 * @author cruise.xu
 * 
 */
public class Query {
	
	private Map<String, Object> queryParams;
	private SimplePage page;

	public Map<String, Object> getQueryParams() {
		return queryParams;
	}

	public void setQueryParams(Map<String, Object> queryParams) {
		this.queryParams = queryParams;
	}

	public SimplePage getPage() {
		return page;
	}

	public void setPage(SimplePage page) {
		this.page = page;
	}
	
	public Query(){}
	
	public Query(SimplePage page){
		this.page = page;
	}
	
	public Query(SimplePage page, Map<String, Object> queryParams){
		this.page = page;
		this.queryParams = queryParams;
	}
	
	public static Query create() {
		return new Query();
	}

	public static Query create(SimplePage page, Map<String, Object> queryParams) {
		return new Query(page, queryParams);
	}
	
	public static Query create(SimplePage page) {
		return new Query(page);
	}
	
	public void addQueryParam(String key, Object value) {
		if(null == queryParams) {
			queryParams = new HashMap<String, Object>();
		}
		queryParams.put(key, value);
	}
	

}