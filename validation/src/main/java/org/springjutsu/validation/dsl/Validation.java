package org.springjutsu.validation.dsl;

import org.springjutsu.validation.rules.RuleErrorMode;

import com.fluentinterface.ReflectionBuilder;

public class Validation {

	public static ValidationEntityBuilder forEntity(Class<?> entityClass) {
		return ReflectionBuilder.implementationFor(ValidationEntityBuilder.class).create().forValidationClass(entityClass);
	}
	
	public static ValidationRuleBuilder rule(String path, String type)
	{
		return ReflectionBuilder.implementationFor(ValidationRuleBuilder.class).create().forPath(path).usingType(type).behaviorOnFail(RuleErrorMode.ERROR);
	}
	
	public static ValidationContextBuilder context(String type, String... qualifiers)
	{
		return ReflectionBuilder.implementationFor(ValidationContextBuilder.class).create().ofType(type).withQualifiers(qualifiers);
	}
	
	public static ValidationContextBuilder group(String... names)
	{
		return ReflectionBuilder.implementationFor(ValidationContextBuilder.class).create().ofType("group").withQualifiers(names);
	}
	
	

}
