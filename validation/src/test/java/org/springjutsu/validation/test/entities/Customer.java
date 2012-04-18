package org.springjutsu.validation.test.entities;

public class Customer {
	
	private String firstName;
	private String lastName;
	private Address address;
	private Address secondaryAddress;
	private Color favoriteColor;
	private String emailAddress;
	private Customer referredBy;
	
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public Address getAddress() {
		return address;
	}
	public void setAddress(Address address) {
		this.address = address;
	}
	public Color getFavoriteColor() {
		return favoriteColor;
	}
	public void setFavoriteColor(Color favoriteColor) {
		this.favoriteColor = favoriteColor;
	}
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	public Customer getReferredBy() {
		return referredBy;
	}
	public void setReferredBy(Customer referredBy) {
		this.referredBy = referredBy;
	}
	public Address getSecondaryAddress() {
		return secondaryAddress;
	}
	public void setSecondaryAddress(Address secondaryAddress) {
		this.secondaryAddress = secondaryAddress;
	}
	
}
