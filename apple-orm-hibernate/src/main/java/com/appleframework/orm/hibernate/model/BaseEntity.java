package com.appleframework.orm.hibernate.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * 实体类 - 基类
 */

@MappedSuperclass
public abstract class BaseEntity implements Serializable {

	private static final long serialVersionUID = -6718838800112233445L;

	public static final String CREATE_TIME_PROPERTY_NAME = "createTime";// "创建日期"属性名称
	public static final String UPDATE_TIME_PROPERTY_NAME = "updateTime";// "修改日期"属性名称

	//protected String gid;
	//protected Long ver;
	protected Date createTime;// 创建日期
	protected Date updateTime;// 修改日期

	//public BaseEntity() {
		//gid = UUID.randomUUID().toString();
		//ver = (long) 0;
	//}
	
	@Transient
	public void onSave() {
	}

	@Transient
	public void onUpdate() {
	}

	@Column(name = "create_time", updatable = false)
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Column(name = "update_time")
	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
	/*@Column(name = "gid", nullable = false, updatable = false, unique = true)
	public String getGid() {
		return gid;
	}*/

	/*@Version
	@Column(name = "ver", nullable = false)
	public long getVer() {
		return ver == null ? 0L : ver;
	}

	public void setVer(Long ver) {
		this.ver = ver;
	}*/

}