package com.appleframework.orm.hibernate.enums;

import org.hibernate.envers.RevisionType;

import com.appleframework.orm.hibernate.exceptions.UnknownEnumValueException;

/**
 * Type of operation(CREATE, UPDATE, DELETE).
 * 
 * @author Cruise.Xu
 * @date: 2011-9-8
 * 
 */
public enum CrudType {
	CREATE, UPDATE, DELETE;

	public static CrudType from(RevisionType type) {
		if (type == null) {
			return null;
		} else if (type == RevisionType.ADD) {
			return CREATE;
		} else if (type == RevisionType.DEL) {
			return DELETE;
		} else if (type == RevisionType.MOD) {
			return UPDATE;
		}
		throw new UnknownEnumValueException(CrudType.class, type.name());
	}
}
