package com.itranswarp.crypto.user;

import java.util.List;

import com.itranswarp.crypto.store.CrudRepository;
import com.itranswarp.crypto.store.Page;
import com.itranswarp.crypto.store.Query;

public interface UserRepository extends CrudRepository<User> {

	@Query("SELECT * FROM User WHERE createdAt > :t")
	Page<User> selectByCreatedAt(long t, int pageNumber, int pageSize);

	@Query("SELECT * FROM User WHERE createdAt > :t1 AND createdAt < :t2")
	List<User> selectByCreatedAt(long t1, long t2);
}
