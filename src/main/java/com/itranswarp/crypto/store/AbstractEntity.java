package com.itranswarp.crypto.store;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class AbstractEntity implements Serializable {

	@Id
	@Column(nullable = false, updatable = false)
	public long id;

	@Column(nullable = false, updatable = false)
	public long createdAt;

	@Column(nullable = false)
	public long updatedAt;

	@Column(nullable = false)
	public long version;

}
