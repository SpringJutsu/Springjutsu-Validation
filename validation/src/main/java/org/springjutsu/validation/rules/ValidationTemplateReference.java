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
	 * Default constructor. Initialize.
	 * @param basePath The Base path.
	 * @param templateName The template name referenced.
	 */
	public ValidationTemplateReference(String basePath, String templateName) {
		this.basePath = basePath;
		this.templateName = templateName;
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
	
}
