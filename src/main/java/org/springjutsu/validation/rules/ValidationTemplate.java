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
import java.util.List;


/**
 * Java representation of an XML validation template.
 * @author Clark Duplichien
 *
 */
public class ValidationTemplate {

	/**
	 * The name of this validation template.
	 */
	protected String name;
	
	/**
	 * The class to which this template applies.
	 */
	protected Class<?> applicableEntityClass;
	
	/**
	 * A list of rules specified for this template.
	 */
	protected List<ValidationRule> rules;
	
	/**
	 * A list of sub templates specified for this template.
	 */
	protected List<ValidationTemplateReference> templateReferences;
	
	/**
	 * Default constructor
	 * @param name Name of this template
	 * @param entityClass class this template applies to
	 */
	public ValidationTemplate(String name, Class entityClass) {
		this.name = name;
		this.applicableEntityClass = entityClass;
		this.rules = new ArrayList<ValidationRule>();
		this.templateReferences = new ArrayList<ValidationTemplateReference>();
	}
	
	/**
	 * Adds a validation rule to the existing set of rules.
	 * @param rule the rule to add.
	 */
	public void addValidationRule(ValidationRule rule) {
		this.rules.add(rule);
	}
	
	/**
	 * Adds a validation template reference to the existing set of references.
	 * @param templateReference the reference to add.
	 */
	public void addTemplateReference(ValidationTemplateReference templateReference) {
		this.templateReferences.add(templateReference);
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the applicableEntityClass
	 */
	public Class<?> getApplicableEntityClass() {
		return applicableEntityClass;
	}
	/**
	 * @param applicableEntityClass the applicableEntityClass to set
	 */
	public void setApplicableEntityClass(Class<?> applicableEntityClass) {
		this.applicableEntityClass = applicableEntityClass;
	}
	/**
	 * @return the rules
	 */
	public List<ValidationRule> getRules() {
		return rules;
	}
	/**
	 * @param rules the rules to set
	 */
	public void setRules(List<ValidationRule> rules) {
		this.rules = rules;
	}
	/**
	 * @return the templateReferences
	 */
	public List<ValidationTemplateReference> getTemplateReferences() {
		return templateReferences;
	}
	/**
	 * @param templateReferences the templateReferences to set
	 */
	public void setTemplateReferences(
			List<ValidationTemplateReference> templateReferences) {
		this.templateReferences = templateReferences;
	}	
}
