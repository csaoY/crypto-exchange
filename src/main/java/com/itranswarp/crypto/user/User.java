package com.itranswarp.crypto.user;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.itranswarp.crypto.store.AbstractEntity;

@Entity
public class User extends AbstractEntity {

	@Column(nullable = false)
	public long groupId;

	@Column(nullable = false)
	public String name;

}
