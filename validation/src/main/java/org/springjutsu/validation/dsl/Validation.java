package org.springjutsu.validation.dsl;

import org.springjutsu.validation.executors.RuleExecutor;
import org.springjutsu.validation.executors.impl.Rules;
import org.springjutsu.validation.rules.RuleErrorMode;

import com.fluentinterface.ReflectionBuilder;

public class Validation {

	public static ValidationEntityBuilder forEntity(Class<?> entityClass) {
		return ReflectionBuilder.implementationFor(ValidationEntityBuilder.class)
				.usingAttributeAccessStrategy(new OverloadedPropertyAwareSetterAttributeAccessStrategy()).create()
				.forValidationClass(entityClass);
	}
	
	public static ValidationRuleBuilder rule(String path, Rules type)
	{
		return rule(path, type.name());
	}
	
	public static ValidationRuleBuilder rule(String path, String type)
	{
		return ReflectionBuilder.implementationFor(ValidationRuleBuilder.class)
				.usingAttributeAccessStrategy(new OverloadedPropertyAwareSetterAttributeAccessStrategy()).create()
				.forPath(path).usingHandler(type).behaviorOnFail(RuleErrorMode.ERROR);
	}
	
	public static ValidationRuleBuilder rule(Object path, Rules type)
	{
		return rule(path, type.name());
	}
	
	public static ValidationRuleBuilder rule(Object path, String type)
	{
		return rule(path.toString(), type);
	}
	
	public static <M,A> ValidationRuleBuilder rule(String path, String message, RuleExecutor<M,A> handlerImpl)
	{
		return ReflectionBuilder.implementationFor(ValidationRuleBuilder.class)
				.usingAttributeAccessStrategy(new OverloadedPropertyAwareSetterAttributeAccessStrategy()).create()
				.forPath(path).withMessage(message).usingHandler(handlerImpl).behaviorOnFail(RuleErrorMode.ERROR);
	}
	
	public static <M,A> ValidationRuleBuilder rule(Object path, String message, RuleExecutor<M,A> handlerImpl)
	{
		return rule(path.toString(), message, handlerImpl);
	}
	public static ValidationContextBuilder context(String type, String... qualifiers)
	{
		return ReflectionBuilder.implementationFor(ValidationContextBuilder.class)
				.usingAttributeAccessStrategy(new OverloadedPropertyAwareSetterAttributeAccessStrategy()).create()
				.ofType(type).withQualifiers(qualifiers);
	}
	
	public static ValidationContextBuilder group(String... names)
	{
		return ReflectionBuilder.implementationFor(ValidationContextBuilder.class)
				.usingAttributeAccessStrategy(new OverloadedPropertyAwareSetterAttributeAccessStrategy()).create()
				.ofType("group").withQualifiers(names);
	}
	
	

}
