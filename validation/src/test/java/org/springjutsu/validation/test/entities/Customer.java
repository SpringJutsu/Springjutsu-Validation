package org.springjutsu.validation.test.entities;

public class Customer extends Person {
	
	private Address address;
	private Address secondaryAddress;
	private Color favoriteColor;
	private String emailAddress;
	private Customer referredBy;
	private Customer copayer;
	private boolean isHappy;

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
	public Customer getCopayer() {
		return copayer;
	}
	public void setCopayer(Customer copayer) {
		this.copayer = copayer;
	}
	public boolean isHappy() {
		return isHappy;
	}
	public void setHappy(boolean isHappy) {
		this.isHappy = isHappy;
	}
}
