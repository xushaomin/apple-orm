package com.appleframework.orm.mybatis.sharding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 分表注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TableRouter {

	/**
	 * 要分表的表名
	 **/
	public String table();

	/**
	 * 要分表的表名和后缀的分隔符
	 **/
	public String decollator() default "";

	/**
	 * 分表依据的参数值
	 **/
	public String paramKey();

	public boolean isAuto() default false;

	/**
	 * 直接指定表的后缀属性值,当参数值中有值时,优先级最高
	 * 
	 * @return
	 */
	public String tableRoute() default "";

	/**
	 * 默认分表总数,只有isAuto设置为true时才生效,默认后缀从0开始
	 **/
	public int count() default 3;

}