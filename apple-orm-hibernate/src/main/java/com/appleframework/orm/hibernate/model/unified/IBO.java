package com.appleframework.orm.hibernate.model.unified;

import java.io.Serializable;
import java.util.Date;

import com.appleframework.orm.hibernate.model.IHasUniqueIdentifier;

/**
 * Business object interface.
 * 
 * @author Cruise.Xu
 * @date: 2011-9-8
 * 
 */
/*public interface IBO extends Serializable, IDirtyLoadableEntity,
		IPrettyPrintableObject, IHibernatePersistentObject, IHasUniqueIdentifier<Long> {*/
	
public interface IBO extends Serializable, IHasUniqueIdentifier<Long> {

	/**
	 * @return persistent class of business object.
	 */
	Class<? extends IBO> getPersistentClass();

	/**
	 * primary key of table.
	 * 
	 * @return unique identifier of this entity.
	 *         <p/>
	 *         This does not mean that this identifier is unique in scope of all
	 *         tables.
	 */
	Long getId();

	/**
	 * global unique identifier.
	 * <p/>
	 * There are situations when you would need to associate different entities
	 * by unique identifier - you can't use primary key of single table in this
	 * case, but rather you can you global unique identifier.
	 * 
	 * @return global unique identifier of this entity.
	 *         <p/>
	 *         This identifier can be used as global primary key of the row
	 *         through all tables.
	 * @see java.util.UUID
	 */
	//String getGid();

	/**
	 * @return logical version of this object.
	 *         <p/>
	 *         optimistic locking version can be used as logical version as they
	 *         are equals in most cases.
	 */
	long getVer();
	
	Date getCreateTime();

	Date getUpdateTime();


	/**
	 * clean fields that were created by default constructor.
	 * <p/>
	 * such entity can be used with find-by-example hibernate method.
	 */
	//void cleanBeanProperties();

	/**
	 * in clustered data distributed system it can be necessary to remove entity
	 * from cache.
	 * <p/>
	 * There are few classes which allow you to clear cache(you can find cash
	 * clear classes in scale4j-rttp module).
	 * <p/>
	 * This method should give an answer for a question - is it time to remove
	 * you class from cache(eligible for cache removal)
	 * 
	 * @return true if this entity can be removed from cache.
	 */
	//boolean isCleanable();
}
