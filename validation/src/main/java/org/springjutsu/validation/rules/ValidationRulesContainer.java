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

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.set.ListOrderedSet;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;
import org.springjutsu.validation.util.PathUtils;

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
public class ValidationRulesContainer {
	
	/**
	 * Bean factory for initializing validation rules container.
	 */
	@Autowired(required=false)
	private List<ValidationEntity> validationEntities = new ArrayList<ValidationEntity>();

	/**
	 * Maps class to the validation entity for that class.
	 */
	private Map<Class<?>, ValidationEntity> validationEntityMap = 
		new HashMap<Class<?>, ValidationEntity>();
	
	/**
	 * Maps template name to template
	 */
	Map<String, ValidationTemplate> validationTemplateMap = 
		new HashMap<String, ValidationTemplate>();
	
	/**
	 * Annotation classes which mark a field that should not be validated recursively.
	 */
	private List<Class<?>> excludeAnnotations = new ArrayList<Class<?>>();
	
	/**
	 * Annotation classes which mark a field that should be validated recursively.
	 */
	private List<Class<?>> includeAnnotations = new ArrayList<Class<?>>();

	public ValidationEntity getValidationEntity(Class<?> clazz) {
		return validationEntityMap.get(clazz);
	}
	
	/**
	 * Inititalizes the validation entity map by scanning for 
	 * @link{ValidationEntity} instances within the application context.
	 * These are registered by class within the map.
	 * This can be a quite expensive initialization, and
	 * will occur during container startup
	 */
	@PostConstruct
	public void initializeValdationEntities() {
		initValidationEntityMap();
		initIncludePaths();
		initExcludePaths();
		initInheritance();
		initRecursivePropertyPaths();
	}
	
	/**
	 * Convert List of entities to a map keyed by the entity's class. 
	 */
	protected void initValidationEntityMap() {
		if (validationEntities == null) {
			validationEntities = new ArrayList<ValidationEntity>();
		}
		for (ValidationEntity validationEntity : validationEntities) {
			validationEntityMap.put(validationEntity.getValidationClass(), validationEntity);
			for (ValidationTemplate template : validationEntity.getValidationTemplates()) {
				validationTemplateMap.put(template.getName(), template);
			}
		}
	}
	
	/**
	 * Read from exclude annotations to further 
	 * populate exclude paths already parsed from XML.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void initExcludePaths() {
		for (ValidationEntity entity : validationEntityMap.values()) {
			// no paths to check on an interface.
			if (entity.getValidationClass().isInterface()) {
				continue;
			}
			BeanWrapper entityWrapper = new BeanWrapperImpl(entity.getValidationClass());
			for (PropertyDescriptor descriptor : entityWrapper.getPropertyDescriptors()) {
				if (!entityWrapper.isReadableProperty(descriptor.getName())
						|| !entityWrapper.isWritableProperty(descriptor.getName())) {
					continue;
				}
				for(Class excludeAnnotation : excludeAnnotations) {
					try {
						if (ReflectionUtils.findField(entity.getValidationClass(), descriptor.getName())
								.getAnnotation(excludeAnnotation) != null) {
							entity.getExcludedPaths().add(descriptor.getName());
						}
					} catch (SecurityException se) {
						throw new IllegalStateException("Unexpected error while checking for excluded properties", se);
					}
				}
			}
		}
	}
	
	/**
	 * Read from include annotations to further 
	 * populate include paths already parsed from XML.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void initIncludePaths() {
		for (ValidationEntity entity : validationEntityMap.values()) {
			// no paths to check on an interface.
			if (entity.getValidationClass().isInterface()) {
				continue;
			}
			BeanWrapper entityWrapper = new BeanWrapperImpl(entity.getValidationClass());
			for (PropertyDescriptor descriptor : entityWrapper.getPropertyDescriptors()) {
				if (!entityWrapper.isReadableProperty(descriptor.getName())
						|| !entityWrapper.isWritableProperty(descriptor.getName())) {
					continue;
				}
				for(Class includeAnnotation : includeAnnotations) {
					try {
						if (ReflectionUtils.findField(entity.getValidationClass(), descriptor.getName())
								.getAnnotation(includeAnnotation) != null) {
							entity.getIncludedPaths().add(descriptor.getName());
						}
					} catch (SecurityException se) {
						throw new IllegalStateException("Unexpected error while checking for included properties", se);
					}
				}
			}
		}
	}
	
	protected void initRecursivePropertyPaths() {
		for (ValidationEntity entity : validationEntityMap.values()) {
			
			if (entity.getValidationClass().isInterface()) {
				continue;
			}
			
			BeanWrapperImpl subBeanWrapper = new BeanWrapperImpl(entity.getValidationClass());
			PropertyDescriptor[] propertyDescriptors = subBeanWrapper.getPropertyDescriptors();
			
			for (PropertyDescriptor property : propertyDescriptors) {
				
				if (!entity.getIncludedPaths().isEmpty() 
						&& !entity.getIncludedPaths().contains(property.getName())) {
					continue;
				}
				
				if (entity.getExcludedPaths().contains(property.getName())) {
					continue;
				}
				
				Class<?> pathClass = PathUtils.getClassForPath(subBeanWrapper.getWrappedClass(), property.getName(), false);
				Class<?> collectionPathClass = PathUtils.getClassForPath(subBeanWrapper.getWrappedClass(), property.getName(), true);
				
				if (this.supportsClass(pathClass)|| 
					(this.supportsClass(collectionPathClass) && 
						(List.class.isAssignableFrom(pathClass) || pathClass.isArray()))) {
					entity.getRecursivePropertyPaths().put(property.getName(), pathClass);
				}
			}
		}
	}

	/**
	 * Copy rules from parent classes into child classes.
	 */
	@SuppressWarnings("unchecked")
	protected void initInheritance() {
		Set<Class<?>> inheritanceChecked = new HashSet<Class<?>>();
		for (ValidationEntity entity : validationEntityMap.values()) {
			
			Stack<Class<?>> classStack = new Stack<Class<?>>();
			classStack.push(entity.getValidationClass());
			for (Class<?> clazz = entity.getValidationClass().getSuperclass(); clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
				classStack.push(clazz);
			}
			
			Set<ValidationRule> inheritableRules = new ListOrderedSet();
			Set<ValidationTemplateReference> inheritableTemplateReferences = new ListOrderedSet();
			Set<ValidationContext> inheritableContexts = new ListOrderedSet();
			Set<String> inheritableExclusionPaths = new HashSet<String>();
			Set<String> inheritableInclusionPaths = new HashSet<String>();
			
			while (!classStack.isEmpty()) {
				Class<?> clazz = classStack.pop();
				if (supportsClass(clazz) && !inheritanceChecked.contains(clazz)) {
					validationEntityMap.get(clazz).getRules().addAll(inheritableRules);
					validationEntityMap.get(clazz).getValidationContexts().addAll(inheritableContexts);
					validationEntityMap.get(clazz).getExcludedPaths().addAll(inheritableExclusionPaths);
					validationEntityMap.get(clazz).getIncludedPaths().addAll(inheritableInclusionPaths);
					validationEntityMap.get(clazz).getTemplateReferences().addAll(inheritableTemplateReferences);
				}
				if (hasRulesForClass(clazz)) {
					inheritableRules.addAll(validationEntityMap.get(clazz).getRules());
				}
				if (supportsClass(clazz)) {
					inheritableContexts.addAll(validationEntityMap.get(clazz).getValidationContexts());
					inheritableExclusionPaths.addAll(validationEntityMap.get(clazz).getExcludedPaths());
					inheritableInclusionPaths.addAll(validationEntityMap.get(clazz).getIncludedPaths());
				}
				inheritanceChecked.add(clazz);
			}
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
	 * @param clazz the class to determine support for.
	 * @return true if an XML validation entity was created
	 * 	 for the specified class. Used during recursive model
	 *   rule validation to determine which fields require 
	 *   nested model validation.
	 */
	public Boolean supportsClass(Class<?> clazz) {
		return getValidationEntity(clazz) != null;
	}
	
	public List<ValidationEntity> getValidationEntities() {
		return validationEntities;
	}

	public void setValidationEntities(List<ValidationEntity> validationEntities) {
		this.validationEntities = validationEntities;
	}
	
	public List<Class<?>> getExcludeAnnotations() {
		return excludeAnnotations;
	}

	public void setExcludeAnnotations(List<Class<?>> excludeAnnotations) {
		this.excludeAnnotations = excludeAnnotations;
	}

	public List<Class<?>> getIncludeAnnotations() {
		return includeAnnotations;
	}

	public void setIncludeAnnotations(List<Class<?>> includeAnnotations) {
		this.includeAnnotations = includeAnnotations;
	}

	public Map<String, ValidationTemplate> getValidationTemplateMap() {
		return validationTemplateMap;
	}

	public void setValidationTemplateMap(
			Map<String, ValidationTemplate> validationTemplateMap) {
		this.validationTemplateMap = validationTemplateMap;
	}
}
