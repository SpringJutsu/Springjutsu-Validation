package org.springjutsu.validation.dsl;

import org.springjutsu.validation.rules.CollectionStrategy;
import org.springjutsu.validation.rules.RuleErrorMode;
import org.springjutsu.validation.rules.ValidationRule;

import com.fluentinterface.builder.Builder;

public interface ValidationRuleBuilder extends Builder<ValidationRule>
{
	public ValidationRuleBuilder forPath(String path);
	public ValidationRuleBuilder usingType(String type);
	public ValidationRuleBuilder withValue(String value);
	public ValidationRuleBuilder withErrorPath(String errorPath);
	public ValidationRuleBuilder withMessage(String message);
	public ValidationRuleBuilder behaviorOnFail(RuleErrorMode onFail);
	public ValidationRuleBuilder withCollectionStrategy(CollectionStrategy collectionStrategy);
	public ValidationRuleBuilder havingValidationContexts(ValidationContextBuilder... contexts);
	public ValidationRuleBuilder havingRules(ValidationRuleBuilder... rules);
	
}