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
	 * Boolean value indicating whether or not rule should
	 * be executed when the path specified in not found
	 * in the Request. Defaults to false.
	 */
	protected boolean validateWhenNotInRequest = false;
	
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
		rule += "/>";
		return rule;
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

	/**
	 * @return whether or not to validate when path is not in request
	 */
	public boolean isValidateWhenNotInRequest() {
		return validateWhenNotInRequest;
	}

	/**
	 * @param set whether or not to validate when path is not in request
	 */
	public void setValidateWhenNotInRequest(boolean validateWhenNotInRequest) {
		this.validateWhenNotInRequest = validateWhenNotInRequest;
	}	
}
