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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springjutsu.validation.exceptions.CircularValidationTemplateReferenceException;

/**
 * This serves as a container for all parsed validation rules.
 * The container serves the rules when requested to the 
 * @link{ValidationManager} implementation.
 * @see{ValidationEntity}
 * @see{ValidationRule}
 * @see{ValidationTemplate}
 * @see{ValidationManager}
 * 
 * @author Clark Duplichien
 * @author Taylor Wicksell
 *
 */
public class ValidationRulesContainer implements BeanFactoryAware {
	
	/**
	 * Bean factory for initializing validation rules container.
	 */
	@Autowired
	private BeanFactory beanFactory;
	
	/**
	 * Maps class to the validation entity for that class.
	 */
	private Map<Class<?>, ValidationEntity> validationEntityMap = null;
	
	public ValidationEntity getValidationEntity(Class<?> clazz) {
		initValidationEntityMap();
		return validationEntityMap.get(clazz);
	}
	
	/**
	 * Inititalizes the validation entity map by scanning for 
	 * @link{ValidationEntity} instances within the application context.
	 * These are registered by class within the map.
	 * This can be a quite expensive initialization, and by default will
	 * occur on the first access. 
	 */
	protected void initValidationEntityMap() {
		if (validationEntityMap == null) {
			validationEntityMap = new HashMap<Class<?>, ValidationEntity>();
			Collection<ValidationEntity> validationEntities = 
				((ListableBeanFactory) beanFactory)
				.getBeansOfType(ValidationEntity.class).values();
			for (ValidationEntity validationEntity : validationEntities) {
				validationEntityMap.put(validationEntity.getValidationClass(), validationEntity);
			}
			unwrapTemplateReferences();
			initRuleInheritance();
		}
	}
	
	/**
	 * Copy rules from parent classes into child classes.
	 */
	protected void initRuleInheritance() {
		Set<Class<?>> inheritanceChecked = new HashSet<Class<?>>();
		for (ValidationEntity entity : validationEntityMap.values()) {
			
			Stack<Class<?>> classStack = new Stack<Class<?>>();
			classStack.push(entity.getValidationClass());
			for (Class<?> clazz = entity.getValidationClass().getSuperclass(); clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
				classStack.push(clazz);
			}
			
			Set<ValidationRule> inheritableRules = new HashSet<ValidationRule>();			
			while (!classStack.isEmpty()) {
				Class<?> clazz = classStack.pop();
				if (supportsClass(clazz) && !inheritanceChecked.contains(clazz)) {
					for (ValidationRule rule : inheritableRules) {
						validationEntityMap.get(clazz).addRule(rule);
					}
				}
				if (hasRulesForClass(clazz)) {
					inheritableRules.addAll(validationEntityMap.get(clazz).getRules());
				}
				inheritanceChecked.add(clazz);
			}
		}
	}
	
	/**
	 * Since use of template references and performing error 
	 * checking on them would otherwise be expensive, unwrap 
	 * all template references into rule sets during initialization.
	 */
	protected void unwrapTemplateReferences() {
		
		Map<String, ValidationTemplate> validationTemplateMap = 
			new HashMap<String, ValidationTemplate>();
		
		// for each validation entity, consolidate validation templates.
		for (ValidationEntity entity : validationEntityMap.values()) {
			for (ValidationTemplate template : entity.getValidationTemplates()) {
				validationTemplateMap.put(template.getName(), template);
			}
		}
		
		// then for each validation entity, unwrap and error check validation templates.
		for (ValidationEntity entity : validationEntityMap.values()) {
			
			// unwrap template references hiding in rules...
			for (ValidationRule rule : entity.getRules()) {
				unwrapValidationRuleTemplateReferences(rule, new Stack<String>(), "", validationTemplateMap);
			}
			
			// unwrap base level template references.
			for (ValidationTemplateReference templateReference : entity.getTemplateReferences()) {
				unwrapTemplateReference(templateReference, entity.getRules(), 
						new Stack<String>(), "", validationTemplateMap);
			}
		}
	}
	
	protected void unwrapTemplateReference(ValidationTemplateReference templateReference, 
			List<ValidationRule> dumpTo, Stack<String> usedNames, String baseName,
			Map<String, ValidationTemplate> validationTemplateMap) {
		
		// illegal recursion check.
		if (usedNames.contains(templateReference.getTemplateName())) {
			usedNames.push(templateReference.getTemplateName());
			throw new CircularValidationTemplateReferenceException(
				"Recursive validation template definition: " + usedNames);
		} else {
			usedNames.push(templateReference.getTemplateName());
		}
		
		// get template
		ValidationTemplate template = validationTemplateMap.get(templateReference.getTemplateName());
		
		// unwrap any template references referenced by this template.
		// dump these unwrapped references into the original rule list.
		for (ValidationTemplateReference subTemplateReference : template.getTemplateReferences()) {
			subTemplateReference.setFormConstraints(templateReference.getFormConstraints());
			unwrapTemplateReference(subTemplateReference, dumpTo, usedNames, 
				appendPath(baseName, templateReference.getBasePath()), validationTemplateMap);
		}
		
		// adapt template rules:
		// check them for template references
		// clone with basename subpath.
		// apply form constraints.
		for (ValidationRule rule : template.getRules()) {
			ValidationRule adaptedRule = rule.cloneWithPath(appendPath(baseName, templateReference.getBasePath(), rule.getPath()));
			adaptedRule.setFormConstraints(templateReference.getFormConstraints());
			unwrapValidationRuleTemplateReferences(adaptedRule, usedNames, 
				appendPath(baseName, templateReference.getBasePath()), validationTemplateMap);
			dumpTo.add(adaptedRule);
		}
		
		// end illegal recursion check.
		usedNames.pop();
	}
	
	protected void unwrapValidationRuleTemplateReferences(ValidationRule rule, 
			Stack<String> usedNames, String baseName, Map<String, ValidationTemplate> validationTemplateMap) {

		// unwrap template references into sub rules.
		for (ValidationTemplateReference subTemplateReference : rule.getTemplateReferences()) {
			unwrapTemplateReference(subTemplateReference, rule.getRules(), usedNames, baseName, validationTemplateMap);
		}
		
		// recursively unwrap any templates within sub rules.
		if (rule.getRules() != null) {
			for (ValidationRule subRule : rule.getRules()) { 
				unwrapValidationRuleTemplateReferences(subRule, usedNames, baseName, validationTemplateMap);
			}
		}
		
		// Don't re-evaluate these template references.
		rule.getTemplateReferences().clear();
	}
	
	protected String appendPath(String... pathSegments) {
		String path = pathSegments[0];
		for (int i = 1; i < pathSegments.length; i++) {
			path += "." + pathSegments[i];
		}
		if (path.startsWith(".")) {
			path = path.substring(1);
		}
		return path;
	}

	/**
	 * Retrieves rules stored for a given class.
	 * @param clazz The class to retrieve rules for.
	 * @param form A string describing the form which the context
	 *  rules are to apply to.
	 * @return the list of validation rules applying to the form and class.
	 */
	public List<ValidationRule> getRules(Class<?> clazz, String form) {
		ValidationEntity entity = getValidationEntity(clazz);
		if (entity != null) {
			return getValidationEntity(clazz).getValidationRules(form);
		} else {
			return new ArrayList<ValidationRule>();
		}
	}
	
	/**
	 * @param clazz The class to check rules for
	 * @return true if there exist model rules for the class.
	 */
	public Boolean hasRulesForClass(Class<?> clazz) {
		ValidationEntity entity = getValidationEntity(clazz);
		return entity != null 
			&& entity.getRules() != null
			&& !entity.getRules().isEmpty();
	}
	
	/**
	 * @param beanFactory the beanFactory to set
	 */
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * @param clazz the class to determine support for.
	 * @return true if an XML validation entity was created
	 * 	 for the specified class. Used during recursive model
	 *   rule validation to determine which fields require 
	 *   nested model validation.
	 */
	public Boolean supportsClass(Class<?> clazz) {
		return getValidationEntity(clazz) != null;
	}
}
