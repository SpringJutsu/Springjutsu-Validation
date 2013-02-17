package org.springjutsu.validation.rules;

import java.util.ArrayList;
import java.util.List;

/**
 * A java representation of an XML template-ref
 * Template references will be unwrapped into rule
 * sets during initialization of the ValidationRulesContainer
 * in order to save on processing time down the road.
 * @author Clark Duplichien
 */
public class ValidationTemplateReference {
	
	/**
	 * The base path representing the object
	 * to be validated by the template.
	 */
	protected String basePath;
	
	/**
	 * The name of the validation template to reference.
	 */
	protected String templateName;
	
	/**
	 * A list of form mappings, if provided, the rule will
	 * only execute when the specified form(s) is/are loaded.
	 */
	protected List<String> formConstraints;
	
	/**
	 * Default constructor. Initialize.
	 * @param basePath The Base path.
	 * @param templateName The template name referenced.
	 */
	public ValidationTemplateReference(String basePath, String templateName) {
		this.basePath = basePath;
		this.templateName = templateName;
		this.formConstraints = new ArrayList<String>();
	}
	
	/** Returns true if the rule applies to the current form.
	 * Replace any REST variable wildcards with wildcard regex.
	 * Replace ant path wildcards with wildcard regexes as well.
	 * Iterate through possible form names to find the first match.
	 */
	public boolean appliesToForm(String form) {
		if (form == null || form.isEmpty()) {
			return true;
		}
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

	/**
	 * @return the basePath
	 */
	public String getBasePath() {
		return basePath;
	}

	/**
	 * @param basePath the basePath to set
	 */
	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	/**
	 * @return the templateName
	 */
	public String getTemplateName() {
		return templateName;
	}

	/**
	 * @param templateName the templateName to set
	 */
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public List<String> getFormConstraints() {
		return formConstraints;
	}

	public void setFormConstraints(List<String> formConstraints) {
		this.formConstraints = formConstraints;
	}
	
}
