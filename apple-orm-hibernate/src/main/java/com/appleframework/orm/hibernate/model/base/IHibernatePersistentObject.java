package com.appleframework.orm.hibernate.model.base;

import java.io.Serializable;

/**
 * 
 * @author Cruise.Xu
 * @date: 2011-9-8
 * 
 */
public interface IHibernatePersistentObject extends Serializable {
	
	/**
	 * Compares object equality.
	 * <p/>
	 * When using Hibernate, the primary key should not be a part of this
	 * comparison or just by primary key.
	 * 
	 * @param o
	 *            object to compare to
	 * @return true/false based on equality tests
	 */
	@Override
	boolean equals(Object o);

	/**
	 * When you override equals, you should override hashCode. See
	 * "Why are equals() and hashCode() importation" for more information:
	 * http://www.hibernate.org/109.html
	 * 
	 * @return hashCode
	 */
	@Override
	int hashCode();
}
