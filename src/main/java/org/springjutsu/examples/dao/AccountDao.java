package org.springjutsu.examples.dao;

import org.springframework.stereotype.Repository;
import org.springjutsu.examples.entities.Account;

@Repository
public class AccountDao extends MapDaoImpl<String, Account> {
	
	public void save(Account entity) {
		super.save(entity.getUsername(), entity);
	}
}
