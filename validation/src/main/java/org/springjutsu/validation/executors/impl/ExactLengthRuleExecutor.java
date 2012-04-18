package org.springjutsu.validation.executors.impl;

import org.springjutsu.validation.executors.ValidWhenEmptyRuleExecutor;
import org.springjutsu.validation.util.ValidationRulesUtils;

/**
 * Asserts that the model's length is exactly  
 * as specified by the argument.
 * @author Clark Duplichien
 * @author Taylor Wicksell
 *
 */
public class ExactLengthRuleExecutor extends ValidWhenEmptyRuleExecutor {
	
	@Override
	public boolean doValidate(Object model, Object argument) {
		return ValidationRulesUtils.getLength(model) == Integer.valueOf(String.valueOf(argument));
	}
	
}
