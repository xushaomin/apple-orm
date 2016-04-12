package com.appleframework.orm.hibernate.model.unified;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.log4j.Logger;


/**
 * Default implementation of IBO interface.
 * <p/>
 * Clients should extend this class.
 * <p/>
 * default hibernate access type is field access
 * 
 * @author Cruise.Xu
 * @date: 2011-9-8
 * @see AbstractPersistentEntity
 * 
 */
@MappedSuperclass
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class BO implements IBO, Cloneable {

	private static final long serialVersionUID = -3433971556160709874L;
	private static Logger logger = Logger.getLogger(BO.class);

	//public static final String PROP_GUID = "gid";
	public static final String PROP_UID = "id";
	public static final String PROP_VERSION = "ver";
	
	public static final String PROP_CREATE_TIME = "createTime";// "创建日期"属性名称
	public static final String PROP_UPDATE_TIME = "updateTime";// "修改日期"属性名称
	
	public static final String ON_SAVE_METHOD_NAME = "onSave";// "保存"方法名称
	public static final String ON_UPDATE_METHOD_NAME = "onUpdate";// "更新"方法名称
	
	//@XmlTransient
	//protected String gid;
	
	@XmlTransient
	protected Long ver;
	
	@XmlTransient
	protected Date createTime;// 创建日期
	
	@XmlTransient
	protected Date updateTime;// 修改日期

	public BO() {
		//gid = UUID.randomUUID().toString();
		ver = (long) 0;
	}

	@Transient
	@Override
	public Class<? extends BO> getPersistentClass() {
		return getClass();
	}

	/*@Override
	@Column(name = "gid", nullable = false, updatable = false, unique = true)
	public String getGid() {
		return gid;
	}*/

	@Override
	@Version
	@Column(name = "ver", nullable = false)
	public long getVer() {
		return ver == null ? 0L : ver;
	}

	/**
	 * internal setter, clients should not use this method directly.
	 * 
	 * @param version
	 *            new version
	 */
	protected void setVer(long ver) {
		this.ver = ver;
	}

	/**
	 * internal setter, clients should not used this method directly.
	 * 
	 * @param guid
	 *            newValue
	 */
	/*protected void setGid(String gid) {
		this.gid = gid;
	}*/
	
	@Override
	@Column(updatable = false)
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Override
	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
	@Transient
	public void onSave() {
	}

	@Transient
	public void onUpdate() {
	}


	/**
	 * @return clone of this object(pre-populated with unique identifier and
	 *         version) that can be used with find-by-example hibernate method.
	 */
	public AbstractPersistentEntity entityForUpdateExample() {
		AbstractPersistentEntity clone = (AbstractPersistentEntity) this.clone();
		//clone.cleanBeanProperties();
		clone.setId(getId());
		clone.setVer(getVer());
		//clone.setGid(getGid());
		return clone;
	}

	/*@Override
	public String reflectionToString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE, false);
	}*/

	/**
	 * hashCode is generated using global_unique_identifier, unique_identifier,
	 * and version fields.
	 * 
	 * @return hash of this object.
	 */
	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(getId());
		builder.append(getVer());
		//builder.append(getGid());
		return builder.toHashCode();
	}

	/**
	 * by default objects are equals if global_unique_identifier,
	 * unique_identifier, and version match.
	 * 
	 * @param obj
	 *            another object of the same class
	 * @return true if equals by global_unique_identifier, unique_identifier,
	 *         and version.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		BO other = (BO) obj;
		EqualsBuilder builder = new EqualsBuilder();
		builder.append(getId(), other.getId());
		builder.append(getVer(), other.getVer());
		//builder.append(getGid(), other.getGid());
		return builder.isEquals();
	}

	@Override
	public BO clone() {
		try {
			logger.debug("cloning this " + this.toString());
			return (BO) super.clone();
		} catch (final CloneNotSupportedException e) {
			logger.error(e);
			throw new NestableRuntimeException(e);
		}
	}

	/**
	 * This method will return short representation of this entity including
	 * class, global_unique_identifier information.
	 * <p/>
	 * NOTE: toString() does not include unique_identifier and version because
	 * it can cause session flushing before transaction commit if somebody will
	 * try to use unique identifier.
	 * 
	 * @return entity representation in format
	 *         [class_name:/global_unique_identifier]
	 */
	@Override
	public String toString() {
		return String.format("%s:/%s", getPersistentClass().getName(), getId());
	}

	/**
	 * by default nothing should prevent us from removing from cache.
	 * <p/>
	 * subclasses must add more useful logic(for example they expect that this
	 * entity will not be used for next 12 h, in this case they can remove this
	 * entity from cache).
	 * <p/>
	 * From the other hand it is possible that clients are expecting that this
	 * entity will be request a lot of time in nearest future(in this case they
	 * would like to leave this entity in cache).
	 * 
	 * @return true
	 */
	/*@Transient
	@Override
	public boolean isCleanable() {
		return true;
	}*/

	/**
	 * set unique identifier. Implementation class can use auto-generated
	 * feature or inject
	 * 
	 * @param uniqueIdentifier
	 */
	public abstract void setId(Long id);
}
