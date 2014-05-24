package org.springjutsu.validation.dsl;

import org.springjutsu.validation.rules.ValidationContext;

import com.fluentinterface.builder.Builder;

public interface ValidationContextBuilder extends Builder<ValidationContext>
{
	public ValidationContextBuilder ofType(String type);
	public ValidationContextBuilder withQualifiers(String... qualifiers);
	public ValidationContextBuilder havingRules(ValidationRuleBuilder... rules);
	
}