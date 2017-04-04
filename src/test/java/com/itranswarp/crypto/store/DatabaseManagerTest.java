package com.itranswarp.crypto.store;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.itranswarp.crypto.user.User;

@Configuration
class DatabaseManagerConfiguration {

	@Bean
	public DatabaseManager createDatabaseManager() {
		return new DatabaseManager();
	}
}

@RunWith(SpringRunner.class)
@Commit
@TestPropertySource("classpath:/test.properties")
@ContextConfiguration(classes = { DatabaseConfiguration.class, DatabaseManagerConfiguration.class })
public class DatabaseManagerTest {

	@Autowired
	DatabaseManager databaseManager;

	@Before
	public void setUp() throws Exception {
		this.databaseManager.dropTable(User.class);
		this.databaseManager.createTable(User.class);
	}

	@Test
	public void testInsert() {
		User user = new User();
		user.id = 1;
		user.groupId = 0;
		user.name = "Hello";
		user.createdAt = user.updatedAt = System.currentTimeMillis();
		user.version = 0;
		this.databaseManager.insert(user);
		assertNotNull(databaseManager);
	}

	@Test
	public void testUpdate() {
		User user = new User();
		user.id = 1;
		user.groupId = 0;
		user.name = "Hello";
		user.createdAt = user.updatedAt = System.currentTimeMillis();
		user.version = 0;
		this.databaseManager.insert(user);
		assertNotNull(databaseManager);
	}

	@Test
	public void testDelete() {
		User user = new User();
		user.id = 1;
		user.groupId = 0;
		user.name = "Hello";
		user.createdAt = user.updatedAt = System.currentTimeMillis();
		user.version = 0;
		this.databaseManager.insert(user);
		assertNotNull(databaseManager);
	}

}
