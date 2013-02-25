package org.springjutsu.validation.rules;

import java.util.List;

/**
 * Holds rules, and any other constructs that go with rules.
 * @author Clark Duplichien
 *
 */
public interface RuleHolder {

	List<ValidationRule> getRules();
	
	List<ValidationTemplateReference> getTemplateReferences();
	
	List<ValidationContext> getValidationContexts();
	
}
