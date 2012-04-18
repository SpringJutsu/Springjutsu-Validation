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

import org.springjutsu.validation.executors.ValidWhenEmptyRuleExecutor;
import org.springjutsu.validation.util.ValidationRulesUtils;

/**
 * Asserts that the model's length is not lesser than 
 * the length specified by the argument.
 * @author Clark Duplichien
 * @author Taylor Wicksell
 *
 */
public class MinLengthRuleExecutor extends ValidWhenEmptyRuleExecutor {
	
	@Override
	public boolean doValidate(Object model, Object argument) {
		return ValidationRulesUtils.getLength(model) >= Integer.valueOf(String.valueOf(argument));
	}

}