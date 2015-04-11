package com.appleframework.orm.hibernate.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

/**
 * 实体类 - 基类
 */

@MappedSuperclass
public class BaseIncrementEntity extends BaseEntity {

	private static final long serialVersionUID = -6718838800112233445L;
	
	protected Long id;// ID

	@Id
	@Column(name = "id", unique = true, nullable = false)
	@GeneratedValue(generator = "generator")
	@GenericGenerator(name = "generator", strategy = "increment")
	public Long  getId() {
		return id;
	}

	public void setId(Long  id) {
		this.id = id;
	}
	
	@Transient
	public void onSave() {
	}

	@Transient
	public void onUpdate() {
	}

	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		}
		if (object instanceof BaseIncrementEntity) {
			BaseIncrementEntity baseEntity = (BaseIncrementEntity) object;
			if (this.getId() == null || baseEntity.getId() == null) {
				return false;
			} else {
				return (this.getId().equals(baseEntity.getId()));
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return id == null ? System.identityHashCode(this) : (this.getClass().getName() + this.getId()).hashCode();
	}
	
}