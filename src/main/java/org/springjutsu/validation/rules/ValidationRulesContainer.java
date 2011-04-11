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
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springjutsu.validation.ValidationEntity;

/**
 * This serves as a container for all parsed validation rules.
 * The container serves the rules when requested to the 
 * @link{ValidationManager} implementation.
 * @see{ValidationEntity}
 * @see{ValidationRule}
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
		}
		return validationEntityMap.get(clazz);
	}
	
	/**
	 * Inititalizes the validation entity map by scanning for 
	 * @link{ValidationEntity} instances within the application context.
	 * These are registered by class within the map.
	 */
	private void initValidationEntityMap() {
		validationEntityMap = new HashMap<Class<?>, ValidationEntity>();
		Collection<ValidationEntity> validationEntities = 
			((ListableBeanFactory) beanFactory)
			.getBeansOfType(ValidationEntity.class).values();
		for (ValidationEntity validationEntity : validationEntities) {
			validationEntityMap.put(validationEntity.getValidationClass(), validationEntity);
		}
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
