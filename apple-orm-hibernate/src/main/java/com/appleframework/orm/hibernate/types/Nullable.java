package com.appleframework.orm.hibernate.types;

public class Nullable extends Condition {
	
	private static final long serialVersionUID = 44044385299131352L;
	
	private boolean isNull;

	public Nullable(String field, boolean isNull) {
		this.field = field;
		this.isNull = isNull;
	}

	public static Nullable isNull(String field) {
		return new Nullable(field, true);
	}

	public static Nullable isNotNull(String field) {
		return new Nullable(field, false);
	}

	public boolean isNull() {
		return isNull;
	}
}
