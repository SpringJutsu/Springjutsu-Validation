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
import java.util.HashSet;
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
public class ValidationContext extends AbstractRuleHolder {
	
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
	private Set<String> qualifiers;
	
	public ValidationContext() {
		this.qualifiers = new HashSet<String>();
		setRules(new ArrayList<ValidationRule>());
		setTemplateReferences(new ArrayList<ValidationTemplateReference>());
		setValidationContexts(new ArrayList<ValidationContext>());
	}

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
	
}
