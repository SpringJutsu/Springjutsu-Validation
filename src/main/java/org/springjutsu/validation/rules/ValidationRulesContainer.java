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
		if (validationEntityMap == null) {
			initValidationEntityMap();
			unwrapTemplateReferences();
			initRuleInheritance();
		}
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
		validationEntityMap = new HashMap<Class<?>, ValidationEntity>();
		Collection<ValidationEntity> validationEntities = 
			((ListableBeanFactory) beanFactory)
			.getBeansOfType(ValidationEntity.class).values();
		for (ValidationEntity validationEntity : validationEntities) {
			validationEntityMap.put(validationEntity.getValidationClass(), validationEntity);
		}
	}
	
	/**
	 * Copy model rules from parent classes into child classes.
	 */
	protected void initRuleInheritance() {
		Set<Class> inheritanceChecked = new HashSet<Class>();
		for (ValidationEntity entity : validationEntityMap.values()) {
			
			Stack<Class> classStack = new Stack<Class>();
			classStack.push(entity.getValidationClass());
			for (Class clazz = entity.getValidationClass().getSuperclass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
				classStack.push(clazz);
			}
			
			List<ValidationRule> inheritableModelRules = new ArrayList<ValidationRule>();			
			while (!classStack.isEmpty()) {
				Class clazz = classStack.pop();
				if (supportsClass(clazz) && !inheritanceChecked.contains(clazz)) {
					for (ValidationRule rule : inheritableModelRules) {
						validationEntityMap.get(clazz).addModelValidationRule(rule);
					}
					inheritableModelRules.clear();
					inheritableModelRules.addAll(validationEntityMap.get(clazz).getModelValidationRules());
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

			// unwrap template references hiding in model form rules....
			List<ValidationRule> unwrappedModelValidationRules = new ArrayList<ValidationRule>();
			for (ValidationRule modelRule : entity.getModelValidationRules()) {
				ValidationRule newModelRule = 
					adaptValidationRuleForTemplateReference(
						modelRule, new Stack<String>(), "", validationTemplateMap);
				unwrappedModelValidationRules.add(newModelRule);
			}
			entity.setModelValidationRules(unwrappedModelValidationRules);
			
			// unwrap template references for model....
			for (ValidationTemplateReference templateReference : entity.getModelValidationTemplateReferences()) {
				unwrapTemplateReference(
						templateReference, entity.getModelValidationRules(), 
						new Stack<String>(), "", validationTemplateMap);
			}
			
			// unwrap template references for context....
			for (String formName : entity.getContextValidationTemplateReferences().keySet()) {
				
				// unwrap template references hiding in context rules...
				List<ValidationRule> unwrappedContextRules = new ArrayList<ValidationRule>();
				for (ValidationRule contextRule : entity.getContextValidationRules().get(formName)) {
					ValidationRule newContextRule = 
						adaptValidationRuleForTemplateReference(
							contextRule, new Stack<String>(), "", validationTemplateMap);
					unwrappedContextRules.add(newContextRule);
				}
				entity.getContextValidationRules().put(formName, unwrappedContextRules);
				
				// unwrap base level template references.
				for (ValidationTemplateReference templateReference : 
					entity.getContextValidationTemplateReferences().get(formName)) {
					unwrapTemplateReference(
							templateReference, entity.getContextValidationRules().get(formName), 
							new Stack<String>(), "", validationTemplateMap);
				}
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
			unwrapTemplateReference(subTemplateReference, dumpTo, usedNames, 
				appendPath(baseName, subTemplateReference.getBasePath()), validationTemplateMap);
		}
		
		// adapt template rules:
		// check them for template references
		// clone with basename subpath.
		for (ValidationRule rule : template.getRules()) {
			ValidationRule adaptedRule = 
				adaptValidationRuleForTemplateReference(rule, usedNames, baseName, validationTemplateMap);
			dumpTo.add(adaptedRule);
		}
		
		// end illegal recursion check.
		usedNames.pop();	
	}
	
	protected ValidationRule adaptValidationRuleForTemplateReference(ValidationRule rule, 
			Stack<String> usedNames, String baseName, Map<String, ValidationTemplate> validationTemplateMap) {
		
		// start a new list of adapted rules.
		List<ValidationRule> adaptedSubRules = new ArrayList<ValidationRule>();

		// dump template references into adapted sub rules.
		for (ValidationTemplateReference subTemplateReference : rule.getTemplateReferences()) {
			unwrapTemplateReference(subTemplateReference, adaptedSubRules, usedNames, 
					appendPath(baseName, subTemplateReference.getBasePath()), validationTemplateMap);
		}
		
		// adapt sub rules into adapted sub rules.
		if (rule.getRules() != null) {
			for (ValidationRule subRule : rule.getRules()) {
				ValidationRule adaptedSubRule = 
					adaptValidationRuleForTemplateReference(subRule, usedNames, baseName, validationTemplateMap);
				adaptedSubRules.add(subRule);
			}
		}
		
		// clone with adapted subpath.
		ValidationRule adaptedRule = rule.cloneWithPath(appendPath(baseName, rule.getPath()));
		
		return adaptedRule;
	}
	
	protected String appendPath(String path, String subPath) {
		String appended = path + "." + subPath;
		if (appended.startsWith(".")) {
			appended = appended.substring(1);
		}
		return appended;
	}

	/**
	 * Retrieves context rules stored for a given class.
	 * @param clazz The class to retrieve rules for.
	 * @param form A string describing the form which the context
	 *  rules are to apply to.
	 * @return the list of validation rules applying to the form and class.
	 */
	public List<ValidationRule> getContextRules(Class<?> clazz, String form) {
		ValidationEntity entity = getValidationEntity(clazz);
		if (entity != null) {
			return getValidationEntity(clazz).getContextValidationRules(form);
		} else {
			return new ArrayList<ValidationRule>();
		}
	}
	
	/**
	 * Retrieves model rules for the specified class
	 * @param clazz The class to retrieve rules for.
	 * @return A list of model rules for the specified class.
	 */
	public List<ValidationRule> getModelRules(Class<?> clazz) {
		ValidationEntity entity = getValidationEntity(clazz);
		if (entity != null) {
			return getValidationEntity(clazz).getModelValidationRules();
		} else {
			return new ArrayList<ValidationRule>();
		}
	}
	
	/**
	 * @param clazz The class to check rules for
	 * @return true if there exist model rules for the class.
	 */
	public Boolean hasModelRulesForClass(Class<?> clazz) {
		ValidationEntity entity = getValidationEntity(clazz);
		return entity != null 
			&& entity.getModelValidationRules() != null
			&& !entity.getModelValidationRules().isEmpty();
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
		if (validationEntityMap == null) {
			initValidationEntityMap();
		}
		return validationEntityMap.containsKey(clazz);
	}
}
