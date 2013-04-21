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

/**
 * Performs validation for a given rule.
 * @author Clark Duplichien
 *
 * @param <M> The type of the model being validated. If the provided model
 *  is not of the requested type, an exception will be thrown.
 * @param <A> The type of the Argument passed to the validation rule. The 
 * configured PropertyEditor and Conversion service beans will be used by
 * a conversion attempt during argument resolution.
 */
public interface RuleExecutor<M, A> {
	
	/**
	 * Validate the given rule
	 * Generally, a rule should return valid if the model is null or empty, 
	 * unless this is a rule to validate the presence of the object.
	 * 
	 * @param model The object on which the rule operates
	 * @param argument Some argument providing details on how to operate on the object
	 * @return true if valid
	 * @throws Exception any exception to be handled by the parent framework.
	 */
	public boolean validate(M model, A argument) throws Exception;

}
