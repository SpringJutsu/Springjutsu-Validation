package org.springjutsu.validation.test.entities;

import org.springjutsu.validation.rules.RecursiveValidationExclude;

public class ExcludeErrorProducingSkippablePerson extends SkippablePerson {

	@RecursiveValidationExclude
	private Customer mismatchedField;
	
	public Customer getMismatchedProperty() {
		return mismatchedField;
	}
	
	public void setMismatchedProperty(Customer mismatchedProperty) {
		this.mismatchedField = mismatchedProperty;
	}
	
}
