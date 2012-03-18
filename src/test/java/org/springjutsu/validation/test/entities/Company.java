package org.springjutsu.validation.test.entities;

import java.util.ArrayList;
import java.util.List;

public class Company {
	
	private String name;
	private List<Customer> customers = new ArrayList<Customer>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Customer> getCustomers() {
		return customers;
	}
	public void setCustomers(List<Customer> customers) {
		this.customers = customers;
	}
}
