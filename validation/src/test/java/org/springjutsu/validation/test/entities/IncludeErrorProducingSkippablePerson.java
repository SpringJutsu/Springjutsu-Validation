package org.springjutsu.validation.test.entities;

import org.springjutsu.validation.rules.RecursiveValidationInclude;

public class IncludeErrorProducingSkippablePerson extends SkippablePerson {

	@RecursiveValidationInclude
	private Customer mismatchedField;
	
	public Customer getMismatchedProperty() {
		return mismatchedField;
	}
	
	public void setMismatchedProperty(Customer mismatchedProperty) {
		this.mismatchedField = mismatchedProperty;
	}
	
}
