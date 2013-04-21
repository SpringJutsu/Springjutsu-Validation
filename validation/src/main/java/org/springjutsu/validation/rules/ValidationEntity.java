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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springjutsu.validation.namespace.ValidationEntityDefinitionParser;

/**
 * A cacheable java description of the XML validation rules.
 * Parsed from XML by @link{ValidationDefinitionParser}, this 
 * class will contain the context validation rules to execute 
 * for a given class, keyed by path, as well as the model rules
 * which are always evaluated for a class.
 * The ValidationEntity objects are stored by the 
 * @link{ValidationRulesContainer} 
 * 
 * @author Clark Duplichien
 * @author Taylor Wicksell
 *
 *@see ValidationRule
 *@see ValidationEntityDefinitionParser
 *@see ValidationRulesContainer
 */
public class ValidationEntity extends AbstractRuleHolder {
	
	/**
	 * Log; for debugging purposes.
	 */
	Log log = LogFactory.getLog(ValidationEntity.class);
	
	/**
	 * A list of paths configured for exclusion from recursive validation.
	 */
	private List<String> excludedPaths;
	
	/**
	 * A list of paths configured for inclusion into recursive validation.
	 */
	private List<String> includedPaths;
	
	/**
	 * A list of validation templates associated with
	 * this entity class.
	 */
	private List<ValidationTemplate> validationTemplates;
	
	/**
	 * The class this set of rules was entered for.
	 */
	private Class<?> validationClass;
	
	/**
	 * Default constructor. Initializes collections.
	 */
	public ValidationEntity() {
		setRules(new ArrayList<ValidationRule>());
		setTemplateReferences(new ArrayList<ValidationTemplateReference>());
		setValidationContexts(new ArrayList<ValidationContext>());
		setValidationTemplates(new ArrayList<ValidationTemplate>());
	}
	
	/**
	 * @return The class these rules are for.
	 */
	public Class<?> getValidationClass() {
		return validationClass;
	}

	/**
	 * @param validationClass The class these rules are for.
	 */
	public void setValidationClass(Class<?> validationClass) {
		this.validationClass = validationClass;
	}

	/**
	 * @return the validationTemplates
	 */
	public List<ValidationTemplate> getValidationTemplates() {
		return validationTemplates;
	}

	/**
	 * @param validationTemplates the validationTemplates to set
	 */
	public void setValidationTemplates(List<ValidationTemplate> validationTemplates) {
		this.validationTemplates = validationTemplates;
	}
	
	public List<String> getExcludedPaths() {
		return excludedPaths;
	}

	public void setExcludedPaths(List<String> excludedPaths) {
		this.excludedPaths = excludedPaths;
	}

	public List<String> getIncludedPaths() {
		return includedPaths;
	}

	public void setIncludedPaths(List<String> includedPaths) {
		this.includedPaths = includedPaths;
	}
}
