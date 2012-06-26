package org.springjutsu.validation.test.entities;

public class Developer extends Person {
	
	Integer coffeesNeeded;

	public Integer getCoffeesNeeded() {
		return coffeesNeeded;
	}

	public void setCoffeesNeeded(Integer coffeesNeeded) {
		this.coffeesNeeded = coffeesNeeded;
	}

}
