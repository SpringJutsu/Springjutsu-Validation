/*
 * Copyright 2010-2011 Duplichien, Wicksell, Springjutsu.org
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

package org.springjutsu.validation.executors.impl;

import org.springjutsu.validation.executors.RuleExecutor;
import org.springjutsu.validation.util.ValidationRulesUtils;

/**
 * Asserts that the model matches the argument in 
 * actual equality or string converted equality.
 * @author Clark Duplichien
 * @author Taylor Wicksell
 * 
 */
public class MatchesRuleExecutor implements RuleExecutor {

	@Override
	public boolean validate(Object model, Object argument) throws Exception {
		if (ValidationRulesUtils.isEmpty(model) && ValidationRulesUtils.isEmpty(argument)) {
			return true;
		} else if (ValidationRulesUtils.isEmpty(model) || ValidationRulesUtils.isEmpty(argument)) {
			return false;
		} else if (model == argument || model.equals(argument) 
			|| String.valueOf(model).equals(String.valueOf(argument))) {
			return true;
		} else {
			return false;
		}
	}
		

}
