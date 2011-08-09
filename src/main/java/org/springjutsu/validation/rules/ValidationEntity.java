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

package org.springjutsu.validation.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class ValidationEntity {
	
	/**
	 * Log; for debugging purposes.
	 */
	Log log = LogFactory.getLog(ValidationEntity.class);

	/**
	 * A list of validation rules to evaluate on the model object.
	 */
	private List<ValidationRule> modelValidationRules;
	
	/**
	 * A list of template references to evaluate on the model object.
	 */
	private List<ValidationTemplateReference> modelValidationTemplateReferences;
	
	/**
	 * A Map of validation rule lists to execute for 
	 * a specific form, keyed by some form path. 
	 */
	private Map<String, List<ValidationRule>> contextValidationRules;
	
	/**
	 * A Map of tempalte references to execute for a specific
	 * form, keyed by some form path.	
	 */
	private Map<String, List<ValidationTemplateReference>> contextValidationTemplateReferences;
	
	/**
	 * A list of validation templates associated with
	 * this entity class.
	 */
	private List<ValidationTemplate> validationTemplates;
	
	/**
	 * The class this set of rules was entered for.
	 */
	private Class validationClass;
	
	/**
	 * Default constructor. Initializes collections.
	 */
	public ValidationEntity() {
		this.modelValidationRules = new ArrayList<ValidationRule>();
		this.modelValidationTemplateReferences = new ArrayList<ValidationTemplateReference>();
		this.contextValidationRules = new HashMap<String, List<ValidationRule>>();
		this.contextValidationTemplateReferences = new HashMap<String, List<ValidationTemplateReference>>();
		this.validationTemplates = new ArrayList<ValidationTemplate>();
	}
	
	/**
	 * Adds a model rule.
	 * @param rule The model rule to add.
	 */
	public void addModelValidationRule(ValidationRule rule) {
		this.modelValidationRules.add(rule);
	}
	
	/**
	 * Add a model template reference.
	 * @param reference The model template reference to add.
	 */
	public void addModelValidationTemplateReference(ValidationTemplateReference reference) {
		this.modelValidationTemplateReferences.add(reference);
	}
	
	/**
	 * Adds a context rule
	 * @param path The path which the rule enforces
	 * @param rule The rule to add. 
	 */
	public void addContextValidationRule(String path, ValidationRule rule) {
		if (!contextValidationRules.containsKey(path)) {
			contextValidationRules.put(path, new ArrayList<ValidationRule>());
		}
		this.contextValidationRules.get(path).add(rule);
	}
	
	public void addContextValidationTemplateReference(String path, ValidationTemplateReference reference) {
		if (!contextValidationTemplateReferences.containsKey(path)) {
			contextValidationTemplateReferences.put(path, new ArrayList<ValidationTemplateReference>());
		}
		this.contextValidationTemplateReferences.get(path).add(reference);
	}
	
	/**
	 * Adds a validation template
	 * @param template the tempalte to add.
	 */
	public void addValidationTemplate(ValidationTemplate template) {
		this.validationTemplates.add(template);
	}
	
	/**
	 * Return the context rules for the given string path.
	 * Replace any REST variable wildcards with wildcard regex.
	 * Replace ant path wildcards with wildcard regexes as well.
	 * Iterate through possible form names to find the first match.
	 * @param form String representing form to get rules for.
	 * @return List of validation rules specific to the form.
	 */
	public List<ValidationRule> getContextValidationRules(String form) {
		List<ValidationRule> formRules = new ArrayList<ValidationRule>();
		for (String formName : contextValidationRules.keySet()) {
			String formPattern = 
				formName.replaceAll("\\{[^\\}]*}", "[^/]+")
				.replaceAll("\\*\\*/?", "(*/?)+")
				.replace("*", "[^/]+");
			if (form.matches(formPattern)) {
				log.debug("Loading rules for form: " + form + ", matched validation rules @ " + formName);
				formRules.addAll(contextValidationRules.get(formName));
			}
		}
		return formRules;
	}
	
	/**
	 * Get the list of model validation rules which are always
	 * run for the model class.
	 * @return List of model validation rules.
	 */
	public List<ValidationRule> getModelValidationRules() {
		return modelValidationRules;
	}

	/**
	 * @param modelValidationRules the modelValidationRules to set
	 */
	public void setModelValidationRules(List<ValidationRule> modelValidationRules) {
		this.modelValidationRules = modelValidationRules;
	}

	/**
	 * @param contextValidationRules the contextValidationRules to set
	 */
	public void setContextValidationRules(Map<String, List<ValidationRule>> contextValidationRules) {
		this.contextValidationRules = contextValidationRules;
	}
	
	/**
	 * @param contextValidationRules the contextValidationRules to set
	 */
	public void addContextValidationRules(String path, List<ValidationRule> contextValidationRuleList) {
		if (!contextValidationRules.containsKey(path)) {
			contextValidationRules.put(path, new ArrayList<ValidationRule>());
		}
		this.contextValidationRules.get(path).addAll(contextValidationRuleList);
	}

	/**
	 * @return The class these rules are for.
	 */
	public Class getValidationClass() {
		return validationClass;
	}

	/**
	 * @param validationClass The class these rules are for.
	 */
	public void setValidationClass(Class validationClass) {
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

	/**
	 * @return the modelValidationTemplateReferences
	 */
	public List<ValidationTemplateReference> getModelValidationTemplateReferences() {
		return modelValidationTemplateReferences;
	}

	/**
	 * @param modelValidationTemplateReferences the modelValidationTemplateReferences to set
	 */
	public void setModelValidationTemplateReferences(
			List<ValidationTemplateReference> modelValidationTemplateReferences) {
		this.modelValidationTemplateReferences = modelValidationTemplateReferences;
	}

	/**
	 * @return the contextValidationTemplateReferences
	 */
	public Map<String, List<ValidationTemplateReference>> getContextValidationTemplateReferences() {
		return contextValidationTemplateReferences;
	}

	/**
	 * @param contextValidationTemplateReferences the contextValidationTemplateReferences to set
	 */
	public void setContextValidationTemplateReferences(
			Map<String, List<ValidationTemplateReference>> contextValidationTemplateReferences) {
		this.contextValidationTemplateReferences = contextValidationTemplateReferences;
	}

	/**
	 * @return the contextValidationRules
	 */
	public Map<String, List<ValidationRule>> getContextValidationRules() {
		return contextValidationRules;
	}
	

}
