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
