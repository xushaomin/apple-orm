package com.appleframework.orm.hibernate.model.unified;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import com.appleframework.orm.hibernate.common.annotation.Field;

/**
 * 
 * @author Cruise.Xu
 * @date: 2011-9-8
 * 
 */
@MappedSuperclass
public abstract class AbstractPersistentEntity extends BO {

	private static final long serialVersionUID = -3859469873970313518L;

	@Field(isId = true)
	private Long id;

	/**
	 * set unique identifier of entity. NO need to populate this field manually
	 * - there is already distributed id generator that will populate this
	 * field.
	 * 
	 * @param uniqueIdentifier
	 *            newValue
	 */
	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	@Id
	@Column(name = "id", nullable = false, updatable = false, unique = true)
	@GeneratedValue(strategy=GenerationType.AUTO)
	public Long getId() {
		return id;
	}
}