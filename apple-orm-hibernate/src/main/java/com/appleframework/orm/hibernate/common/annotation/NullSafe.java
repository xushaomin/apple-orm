package com.appleframework.orm.hibernate.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is just to increase readability of code.
 * <p/>
 * Marking method with this annotation means that method can 'work' with null references and is
 * expecting for such situations.
 * <p/>
 * Client should not worry when passing null reference to this such methods.
 * 
 * @author Cruise.Xu
 * @date: 2012-10-15
 *
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface NullSafe {
}
