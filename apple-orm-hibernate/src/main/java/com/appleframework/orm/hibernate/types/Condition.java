package com.appleframework.orm.hibernate.types;

import java.io.Serializable;

public class Condition implements Serializable {

	private static final long serialVersionUID = 354206235268766950L;
	
	protected String field;

	public String getField() {
		return field;
	}

}
