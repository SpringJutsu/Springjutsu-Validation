package org.springjutsu.validation.dsl;

import org.springjutsu.validation.rules.ValidationEntity;

import com.fluentinterface.builder.Builder;

public interface ValidationEntityBuilder extends Builder<ValidationEntity>
{
	ValidationEntityBuilder forValidationClass(Class<?> validationClass);
	public ValidationEntityBuilder havingIncludedPaths(String... includedPaths);
	public ValidationEntityBuilder havingExcludedPaths(String... excludedPaths);
	public ValidationEntityBuilder havingRules(ValidationRuleBuilder... rules);
	public ValidationEntityBuilder havingValidationContexts(ValidationContextBuilder... contexts);
}