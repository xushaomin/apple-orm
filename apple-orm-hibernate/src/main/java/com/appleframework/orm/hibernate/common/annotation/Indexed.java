package com.appleframework.orm.hibernate.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Indexed {

	/**
	 * 类在索引中的名称，若不指定，默认使用类名。
	 */
	String name() default "";

}