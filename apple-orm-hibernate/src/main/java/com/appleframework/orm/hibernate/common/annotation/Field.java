package com.appleframework.orm.hibernate.common.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ METHOD, FIELD })
@Retention(RUNTIME)
public @interface Field {
	/**
	 * 名称，若不指定，默认使用属性名。
	 */
	String name() default "";

	boolean isId() default false;

}