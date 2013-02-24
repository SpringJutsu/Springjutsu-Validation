package org.springjutsu.validation.rules;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A ValidationContext defines some specific environment
 * or context for validation which should encapsulate 
 * rules specific to an expected event or occurrence
 * in a user's application processing.
 * Examples include validation of specific forms within
 * a web environment, validation of beans unmarshalled
 * from SOAP web service requests, validation of beans
 * read from a serialized source such as an XML or CSV
 * import, etc etc etc.
 * @author Clark Duplichien
 *
 */
public class ValidationContext implements RuleHolder {
	
	/**
	 * The context type should identify the type of
	 * event or contextual occurrence which is 
	 * expected to occur at some point of application
	 * processing. This could be "form" or "endpoint"
	 * or something of that sort. This will match 
	 * the event type declared by a ValidationContextHandler. 
	 */
	private String type;
	
	/**
	 * The qualifier(s) should identify the specific
	 * event or occurrence that should be handled.
	 * This may be the path of a specific form, or
	 * the name of a specific web service endpoint,
	 * or something of that sort. This is used to 
	 * logically separate rule groups for specific
	 * events.
	 */
	private Set<String> qualifiers = new HashSet<String>();
	
	/**
	 * The validation rules that should be run when
	 * this event is handled by the associated 
	 * ValidationContextHandler.
	 */
	private List<ValidationRule> rules;
	
	/**
	 * The validation template references that
	 * should be evaluated when this event is 
	 * handled by the associated
	 * ValidationContextHandler.
	 */
	private List<ValidationTemplateReference> templateReferences;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Set<String> getQualifiers() {
		return qualifiers;
	}

	public void setQualifiers(Set<String> qualifiers) {
		this.qualifiers = qualifiers;
	}

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

}
