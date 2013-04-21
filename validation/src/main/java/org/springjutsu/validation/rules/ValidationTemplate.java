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


/**
 * Java representation of an XML validation template.
 * @author Clark Duplichien
 *
 */
public class ValidationTemplate extends AbstractRuleHolder {

	/**
	 * The name of this validation template.
	 */
	protected String name;
	
	/**
	 * The class to which this template applies.
	 */
	protected Class<?> applicableEntityClass;
	
	/**
	 * Default constructor
	 * @param name Name of this template
	 * @param entityClass class this template applies to
	 */
	public ValidationTemplate(String name, Class<?> entityClass) {
		this.name = name;
		this.applicableEntityClass = entityClass;
		setRules(new ArrayList<ValidationRule>());
		setTemplateReferences(new ArrayList<ValidationTemplateReference>());
		setValidationContexts(new ArrayList<ValidationContext>());
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
	
}
