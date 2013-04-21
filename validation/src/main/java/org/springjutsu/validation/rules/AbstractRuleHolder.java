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

package org.springjutsu.validation.rules;

import java.util.List;

public class AbstractRuleHolder implements RuleHolder {
	
	/**
	 * A list of validation rules to evaluate on the model object.
	 */
	private List<ValidationRule> rules;
	
	/**
	 * A list of template references to evaluate on the model object.
	 */
	private List<ValidationTemplateReference> templateReferences;
	
	/**
	 * A list of contexts which conditionally apply additional rules.
	 */
	private List<ValidationContext> validationContexts;
	

	public List<ValidationRule> getRules() {
		return rules;
	}

	public void setRules(List<ValidationRule> rules) {
		this.rules = rules;
	}

	public List<ValidationTemplateReference> getTemplateReferences() {
		return templateReferences;
	}

	public void setTemplateReferences(
			List<ValidationTemplateReference> templateReferences) {
		this.templateReferences = templateReferences;
	}

	public List<ValidationContext> getValidationContexts() {
		return validationContexts;
	}

	public void setValidationContexts(List<ValidationContext> validationContexts) {
		this.validationContexts = validationContexts;
	}
	
	

}
