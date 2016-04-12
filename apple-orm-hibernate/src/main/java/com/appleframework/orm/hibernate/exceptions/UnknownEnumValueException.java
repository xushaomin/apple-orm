package com.appleframework.orm.hibernate.exceptions;

/**
 * this exception created to help handing database java enum mapping
 * inconsistency.
 * 
 * @author Cruise.Xu
 * @date: 2011-9-8
 * 
 */
public class UnknownEnumValueException extends RuntimeException {
	
	private static final long serialVersionUID = 8088442578739491290L;

	private final Class<?> enumClass;
	private final String dbValue;

	public UnknownEnumValueException(final Class<?> enumClass, final String dbValue) {
		assert enumClass != null;
		this.enumClass = enumClass;
		this.dbValue = dbValue;
	}

	@Override
	public String getMessage() {
		return String.format("Unable to identify correct enum of class[%s] for db value[%s]", enumClass, dbValue);
	}
}
