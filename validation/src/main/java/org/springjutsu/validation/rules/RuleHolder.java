package org.springjutsu.validation.rules;

import java.util.List;

/**
 * Holds rules, and any other constructs that go with rules.
 * For now that's just rules and template references.
 * @author Clark Duplichien
 *
 */
public interface RuleHolder {

	List<ValidationRule> getRules();
	
	List<ValidationTemplateReference> getTemplateReferences();
	
}
