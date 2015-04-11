/*
 * ValueObject.java
 *
 * 深圳广联赛讯有限公司
 *
 * Copyright (C) 2012 WONDERSHARE.COM
 *
 * All Right reserved
 * http://www.glsx.com.cn
 */
package com.appleframework.orm.hibernate.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifying this annotation means that class is just value object without any logic.
 * <p/>
 * Properties of such class allowed to be public to save developer's time.
 * 
 * @author Cruise.Xu
 * @date: 2012-10-15
 *
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ValueObject {
}