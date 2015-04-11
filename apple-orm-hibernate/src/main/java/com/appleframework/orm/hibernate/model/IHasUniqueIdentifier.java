/*
 * IHasUniqueIdentifier.java
 *
 * 深圳广联赛讯有限公司
 *
 * Copyright (C) 2012 WONDERSHARE.COM
 *
 * All Right reserved
 * http://www.glsx.com.cn
 */
package com.appleframework.orm.hibernate.model;
/**
 * Marker interface - implementing means that object has unique identifier of long type.
 * @author Cruise.Xu
 * @date: 2012-10-15
 *
 */
public interface IHasUniqueIdentifier<T> {
	 /**
	    * unique identifier of entity.
	    * 
	    * @return identifier of revision.
	    */
	   T getId();
}

