/*
 * Copyright 2010-2013 Duplichien, Wicksell, Springjutsu.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springjutsu.validation.executors;

import org.springjutsu.validation.util.ValidationRulesUtils;

/**
 * Returns as valid if model object is null or empty,
 * thereby performing an initial null check, and only validating
 * non-empty models.
 * @author Clark Duplichien
 *
 */
public abstract class ValidWhenEmptyRuleExecutor<M, A> implements RuleExecutor<M, A> {

	public boolean validate(M model, A argument) {
		return ValidationRulesUtils.isEmpty(model) || doValidate(model, argument);
	}
	
	/**
	 * Implementors can use this to actually do validation.
	 * @param model Model to validate
	 * @param argument Argument to the rule
	 * @return true if the rule passed.
	 */
	public abstract boolean doValidate(M model, A argument);
	
}
