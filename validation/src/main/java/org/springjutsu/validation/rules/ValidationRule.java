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

import org.springjutsu.validation.util.PathUtils;

/**
 * Java representation of an XML validation rule.
 * @author Clark Duplichien
 * @author Taylor Wicksell
 *
 */
public class ValidationRule {
	
	/**
	 * Path to the field to validate.
	 */
	protected String path;
	
	/**
	 * Name of the rule executor, implies the 
	 * type of rule to apply.
	 */
	protected String type;
	
	/**
	 * This is the argument to be passed
	 * to the rule executor.
	 */
	protected String value;
	
	/**
	 * An optional message code which may be used to 
	 * resolve a non-default error message (a message 
	 * other than that which is coded for the rule executor). 
	 */
	protected String message;
	
	/**
	 * The path on which the error should be written 
	 * on the @link{Errors} object. In case you want to 
	 * validate one field, but put the error message onto
	 * another field.
	 */
	protected String errorPath;
	
	/**
	 * Indicates whether the rule should be applied to individual
	 * collection members, or to the collection object itself.
	 */
	protected CollectionStrategy collectionStrategy;
	
	/**
	 * A list of form mappings, if provided, the rule will
	 * only execute when the specified form(s) is/are loaded.
	 */
	protected List<String> formConstraints;

	/**
	 * These are any validation rules that were nested
	 * within the current rule in XML. If nested rules exist
	 * and this rule passed, the nested rules would be run.
	 * If nested rules exist, an error is not recorded when
	 * this rule fails.
	 */
	protected List<ValidationRule> rules;
	
	/**
	 * These are any validation template references
	 * that were nested within the current rule in XML.
	 * If nested templates exist and this rule passed, the
	 * nested templates would be run. If nested templates
	 * exist, an error is not recorded when this rule fails.
	 */
	protected List<ValidationTemplateReference> templateReferences;
	
	/**
	 * Default constructor, utilized by @link{ValidationDefinitionParser}
	 * @param path See path docs.
	 * @param type See type docs.
	 * @param value See value docs.
	 */
	public ValidationRule(String path, String type, String value) {
		this.path = path;
		this.type = type;
		this.value = value;
		this.rules = new ArrayList<ValidationRule>();
		this.formConstraints = new ArrayList<String>();
		this.templateReferences = new ArrayList<ValidationTemplateReference>();
	}
	
	/**
	 * Clones this validation rule but with a different path
	 * Used within validation logic of @link{ValidationManager}
	 * @param path The new path to apply to the cloned rule
	 * @return A cloned rule with the new path.
	 */
	public ValidationRule cloneWithPath(String path) {
		ValidationRule newRule = new ValidationRule(path, this.type, this.value);
		newRule.setErrorPath(this.errorPath);
		newRule.setMessage(this.message);
		newRule.getRules().addAll(this.rules);
		newRule.getTemplateReferences().addAll(this.templateReferences);
		return newRule;
	}
	
	/**
	 * Clones this validation rule but with a different base path
	 * Used within validation logic of @link{ValidationManager}
	 * The base path is also applied to all sub rules recursively.
	 * @param path The new path to apply to the cloned rule
	 * @return A cloned rule with the new path.
	 */
	public ValidationRule cloneWithBasePath(String basePath) {
		ValidationRule newRule = new ValidationRule(PathUtils.appendPath(basePath, path), this.type, this.value);
		newRule.setErrorPath(this.errorPath);
		newRule.setMessage(this.message);
		for (ValidationRule rule : this.rules) {
			newRule.getRules().add(rule.cloneWithBasePath(basePath));
		}
		newRule.getTemplateReferences().addAll(this.templateReferences);
		return newRule;
	}
	
	/**
	 * @return true if there are nested validation rules.
	 */
	public boolean hasChildren() {
		return this.rules != null && !this.rules.isEmpty();
	}
	
	/**
	 * The toString() representation is a reconstruction of 
	 * the XML syntax of the validation rule, minus any
	 * nested validation rules.
	 */
	@Override
	public String toString() {
		String rule = "<rule ";
		if (path != null && path.length() > 0) {
			rule += "path=\"" + path + "\" ";
		}
		if (type != null && type.length() > 0) {
			rule += "type=\"" + type + "\" ";
		}
		if (value != null && value.length() > 0) {
			rule += "value=\"" + value + "\" ";
		}
		if (message != null && message.length() > 0) {
			rule += "message=\"" + message + "\" "; 
		}
		if (errorPath != null && errorPath.length() > 0) {
			rule += "errorPath=\"" + errorPath + "\" "; 
		}
		if (collectionStrategy != null) {
			rule += "collectionStrategy=\"" + collectionStrategy.getXmlValue() + "\" ";
		}
		rule += "/>";
		return rule;
	}
	
	/** Returns true if the rule applies to the current form.
	 * Replace any REST variable wildcards with wildcard regex.
	 * Replace ant path wildcards with wildcard regexes as well.
	 * Iterate through possible form names to find the first match.
	 */
	public boolean appliesToForm(String form) {
		boolean appliesToForm = formConstraints.isEmpty();
		for (String formName : formConstraints) {
			String formPattern = 
				formName.replaceAll("\\{[^\\}]*}", "[^/]+")
				.replaceAll("\\*\\*/?", "(*/?)+")
				.replace("*", "[^/]+");
			if (form.matches(formPattern)) {
				appliesToForm = true;
				break;
			}			
		}
		return appliesToForm;
	}
	
	public void addFormConstraint(String form) {
		this.formConstraints.add(form);
	}
	
	/**
	 * Adds a rule to the nested validation rules.
	 * @param rule The rule to add.
	 */
	public void addRule(ValidationRule rule)
	{
		this.rules.add(rule);
	}
	
	/**
	 * Adds a template ref to the nested validation template references.
	 * @param templateReference The template reference to add.
	 */
	public void addTemplateReference(ValidationTemplateReference templateReference) {
		this.templateReferences.add(templateReference);
	}
	
	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set.
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the value / argument
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value / argument to set
	 */
	public void setValue(String value) {
		this.value = value;
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
	 * @return the message
	 */
	public synchronized String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public synchronized void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the errorPath
	 */
	public String getErrorPath() {
		return errorPath;
	}

	/**
	 * @param errorPath the errorPath to set
	 */
	public void setErrorPath(String errorPath) {
		this.errorPath = errorPath;
	}
	
	public CollectionStrategy getCollectionStrategy() {
		return collectionStrategy;
	}

	public void setCollectionStrategy(CollectionStrategy collectionStrategy) {
		this.collectionStrategy = collectionStrategy;
	}

	public List<String> getFormConstraints() {
		return formConstraints;
	}

	public void setFormConstraints(List<String> formConstraints) {
		this.formConstraints = formConstraints;
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
