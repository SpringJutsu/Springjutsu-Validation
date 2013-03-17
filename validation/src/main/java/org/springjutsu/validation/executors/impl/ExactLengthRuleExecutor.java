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
public class ExactLengthRuleExecutor extends ValidWhenEmptyRuleExecutor<Object, Integer> {
	
	@Override
	public boolean doValidate(Object model, Integer argument) {
		return ValidationRulesUtils.getLength(model) == argument;
	}
	
}
