package com.appleframework.orm.mybatis.sharding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * mybaties主库注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Master {

	public static final String MYCAT_MASTER = "/*!mycat:db_type=master*/";

	public static final String MAX_SCALE_MASTER = "; -- maxscale route to master";

	String masterSql() default MAX_SCALE_MASTER;

	String masterKey() default "";

	PositionType position() default PositionType.After;
}
