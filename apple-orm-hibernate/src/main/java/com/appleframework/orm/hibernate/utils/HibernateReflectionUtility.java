package com.appleframework.orm.hibernate.utils;

import java.lang.reflect.Field;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.Transient;

import org.apache.commons.lang.ArrayUtils;
/**
 *
 * @author Cruise.Xu
 * @date: 2011-9-8
 *
 */
public final class HibernateReflectionUtility {
	/**
	    * identifies if the given field is persistent field, which is tracked by hibernate.
	    * 
	    * @param field
	    *           field.
	    * @return true if field is not marked with {@link Transient} annotation and contains annotation
	    *         {@link Column} or {@link JoinColumn} or {@link Embedded}, else false.
	    */
	   public static boolean isPersistentField(final Field field) {
	      return (field.getAnnotation(Column.class) != null
	               || field.getAnnotation(JoinColumn.class) != null
	               || field.getAnnotation(Embedded.class) != null || field
	               .getAnnotation(Enumerated.class) != null)
	               && (field.getAnnotation(Transient.class) == null);
	   }

	   /**
	    * this method can be used to modify the state of particular property.
	    * 
	    * @param currentState
	    *           array of property values
	    * @param propertyNames
	    *           array of property names
	    * @param propertyToSet
	    *           property to be modified
	    * @param value
	    *           newValue to set
	    */
	   public static void setValue(Object[] currentState, String[] propertyNames, String propertyToSet,
	            Object value) {
	      int index = ArrayUtils.indexOf(propertyNames, propertyToSet);
	      if (index >= 0) {
	         currentState[index] = value;
	      }
	   }

	   private HibernateReflectionUtility() {
	   }
}

