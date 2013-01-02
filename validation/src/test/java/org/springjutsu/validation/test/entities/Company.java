package org.springjutsu.validation.test.entities;

import java.util.ArrayList;
import java.util.List;

public class Company {
	
	private String name;
	private List<Customer> customers = new ArrayList<Customer>();
	private List<Company> acquisitions = new ArrayList<Company>();
	private List<String> slogans = new ArrayList<String>();
	private List<String> websites = new ArrayList<String>();

	public List<String> getSlogans() {
		return slogans;
	}
	public void setSlogans(List<String> slogans) {
		this.slogans = slogans;
	}
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
	public List<Company> getAcquisitions() {
		return acquisitions;
	}
	public void setAcquisitions(List<Company> acquisitions) {
		this.acquisitions = acquisitions;
	}
	public List<String> getWebsites() {
		return websites;
	}
	public void setWebsites(List<String> websites) {
		this.websites = websites;
	}
}
