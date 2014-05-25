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

package org.springjutsu.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.SingletonMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.convert.ConversionService;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.beanvalidation.CustomValidatorBean;
import org.springjutsu.validation.context.ValidationContextHandler;
import org.springjutsu.validation.context.ValidationContextHandlerContainer;
import org.springjutsu.validation.executors.RuleExecutor;
import org.springjutsu.validation.executors.RuleExecutorContainer;
import org.springjutsu.validation.rules.CollectionStrategy;
import org.springjutsu.validation.rules.RuleErrorMode;
import org.springjutsu.validation.rules.RuleHolder;
import org.springjutsu.validation.rules.ValidationContext;
import org.springjutsu.validation.rules.ValidationEntity;
import org.springjutsu.validation.rules.ValidationRule;
import org.springjutsu.validation.rules.ValidationRulesContainer;
import org.springjutsu.validation.rules.ValidationTemplate;
import org.springjutsu.validation.rules.ValidationTemplateReference;
import org.springjutsu.validation.util.PathUtils;

/**
 * Registerable as a JSR-303 @link{CustomValidatorBean}, this 
 * ValidationManager class is instead responsible for reading
 * XML-driven nested validation rules.
 * However, it populates a standard Errors object as expected.
 *  
 * @author Clark Duplichien
 * @author Taylor Wicksell
 *
 *@see CustomValidatorBean
 */
public class ValidationManager extends CustomValidatorBean {
	
	/**
	 * Validate a lot of requests. A log is fine, too.
	 */
	protected static Log log = LogFactory.getLog(ValidationManager.class);

	/**
	 * Holds the validation rules which have been 
	 * parsed from the XML rule sets.
	 * @see ValidationRulesContainer
	 * @see ValidationRule
	 */
	@Autowired
	protected ValidationRulesContainer rulesContainer;

	/**
	 * Holds the implementations of the validation
	 * rule executors.
	 * @see RuleExecutorContainer
	 * @see RuleExecutor
	 */
	@Autowired
	protected RuleExecutorContainer ruleExecutorContainer;
	
	/**
	 * Holds the handlers for specific validation contexts
	 * @see ValidationContextHandlerContainer
	 * @see ValidationContext
	 */
	@Autowired
	protected ValidationContextHandlerContainer contextHandlerContainer;
	
	/**
	 * Handles logging validation errors against an Errors object,
	 * as well as looking up the appropriate message codes to describe
	 * the rule that failed, and the object(s) that failed validation.
	 * @see ValidationErrorMessageHandler
	 */
	@Autowired
	protected ValidationErrorMessageHandler validationErrorMessageHandler;
	
	/**
	 * Used by the TypeConverter to convert validation arguments
	 * to the type specified by RuleExecutor parameterization.
	 */
	@Autowired(required=false)
	protected ConversionService conversionService;
	
	/**
	 * Used by the TypeConverter to convert validation arguments
	 * to the type specified by RuleExecutor parameterization.
	 */
	protected TypeConverter typeConverter;
	
	@Override
	public boolean supports(Class<?> clazz) {
		return rulesContainer.supportsClass(clazz);
	}
	
	/**
	 * Hook point to perform validation without a web request.
	 * Create and return a BindingModel to allow users to 
	 * manage errors.
	 */
	public Errors validate(Object target) {
		return validate(target, new Object[]{});
	}
	
	/**
	 * Hook point to perform validation without a web request,
	 * but with specific JSR-303 groups.
	 */
	public Errors validate(Object target, Object... validationHints) {
		Errors errors = new BeanPropertyBindingResult(target, "validationTarget");
		validate(target, errors, validationHints);
		return errors;
	}
	
	/**
	 * Validation entry point defined in SpringValidatorAdapter
	 */
	@Override
	public void validate(Object target, Errors errors) {
		validate(target, errors, new Object[]{});
	}

	/**
	 * Validation entry point defined in SpringValidatorAdapter
	 * Validation Hints are really a JSR-303 construct for validation groups,
	 * and should be handled by issue #19 which would allow for delegation
	 * to other validation managers to handle mixing JSR-303 annotations
	 * with Springjutsu validation XMLs.
	 */
	@Override
	public void validate(Object target, Errors errors, Object... validationHints) {
		doValidate(new ValidationEvaluationContext(target, errors, validationHints));
	}
	
	/**
	 * Method responsible for actually executing validation rules.
	 * Invokes contextual rules, then performs recursive sub-bean validation
	 * on eligible sub bean validation paths by calling itself recursively
	 * after pushing the sub bean path onto the ValidationEvaluationContext.
	 * @param context the current context object indicating which path is currently
	 * being validated.
	 */
	protected void doValidate(ValidationEvaluationContext context) {
		if (log.isDebugEnabled()) {
			String nestedPath = PathUtils.joinPathSegments(context.getNestedPath());
			log.debug("Current recursion path is: " + (nestedPath.isEmpty() ? "root object" : nestedPath));
		}
		if (context.getModelWrapper() == null) {
			if (log.isDebugEnabled()) {
				log.debug("Attempted to validate null object, skipping.");
			}
			return;
		}
		
		Object validateMe = context.getBeanAtNestedPath();
		if (log.isDebugEnabled()) {
			log.debug("Found object to validate: " + String.valueOf(validateMe));
		}
		
		// Infinite recursion check
		if (validateMe == null || context.previouslyValidated(validateMe)) {
			if (log.isDebugEnabled()) {
				log.debug("Already validated this object on current path structure, skipping to prevent infinite recursion.");
			}
			return;
		} else {
			context.markValidated(validateMe);
		}
		
		ValidationEntity validationEntity = rulesContainer.getValidationEntity(validateMe.getClass());
		
		callRules(context, validationEntity);
		 
		// Get fields for subbeans and iterate
		BeanWrapperImpl subBeanWrapper = new BeanWrapperImpl(validateMe);
		
		for (Map.Entry<String, Class<?>> recursionPath : validationEntity.getRecursivePropertyPaths().entrySet()) {
			
			if (List.class.isAssignableFrom(recursionPath.getValue()) || recursionPath.getValue().isArray()) {
				Object potentialList = subBeanWrapper.getPropertyValue(recursionPath.getKey());
				List<?> list = (List<?>) (recursionPath.getValue().isArray() && potentialList != null 
					? Arrays.asList(potentialList) : potentialList);
				
				if (list == null || list.isEmpty()) {
					continue;
				}
				
				for (int i = 0; i < list.size(); i++) {
					String nestedPathSegment = recursionPath.getKey() + "[" + i + "]";
					if (log.isDebugEnabled()) {
						log.debug("Pushing nested path: " + nestedPathSegment);
					}
					
					context.pushNestedPath(nestedPathSegment);
					doValidate(context);
					context.popNestedPath();
					log.debug("Done validating nested path: " + nestedPathSegment);
				}
				
			} else {
				context.pushNestedPath(recursionPath.getKey());
				doValidate(context);
				context.popNestedPath();
			}
		}
		
		if (log.isDebugEnabled()) {
			String nestedPath = PathUtils.joinPathSegments(context.getNestedPath());
			log.debug("Done validating recursion path: " + (nestedPath.isEmpty() ? "root object" : nestedPath));
		}
	}
	
	/**
	 * Responsible for invoking all validation rules within the given rule holder.
	 * Invokes non-scoped validation rules, template-scoped validation rules,
	 * and then context-scoped validation rules in turn.
	 * @param context The validation context object which indicates the current object
	 * against which the rules should be evaluated. 
	 * @param ruleHolder could be the base validation entity, a validation rule that passed,
	 * a validation template, or a validation context.
	 */
	@SuppressWarnings("unchecked")
	protected void callRules(ValidationEvaluationContext context, RuleHolder ruleHolder) {
		for (ValidationRule rule : ruleHolder.getRules()) {
			
			// break down any collections into indexed paths.
			SingletonMap collectionReplacements = resolveCollectionPathReplacements(context, rule);
			
			// if there are no collection replacements to be made, 
			// run the rule (and any sub rules) as normal.
			if (collectionReplacements == null) {
				handleValidationRule(context, rule);
			} else {
				// Otherwise, iterate through the collection replacements,
				// and run the rule (and any sub rules) for each base path.
				String collectionReplacementKey = (String) collectionReplacements.getKey();
				List<String> collectionReplacementValues = (List<String>) collectionReplacements.getValue();
				
				for (String collectionReplacementValue : collectionReplacementValues) {
					context.getCollectionPathReplacements()
						.put(collectionReplacementKey, collectionReplacementValue);
					handleValidationRule(context, rule);
					context.getCollectionPathReplacements().remove(collectionReplacementKey);
				}
			}
		}
		for (ValidationTemplateReference templateReference : ruleHolder.getTemplateReferences()) {
			ValidationTemplate actualTemplate = 
				rulesContainer.getValidationTemplateMap().get(templateReference.getTemplateName());
			context.pushTemplate(templateReference, actualTemplate);
			callRules(context, actualTemplate);
			context.popTemplate();
		}
		
		for (ValidationContext validationContext : ruleHolder.getValidationContexts()) {
			ValidationContextHandler contextHandler = 
				contextHandlerContainer.getContextHandlerForType(validationContext.getType());
			
			// if we're performing sub bean validation,
			// and the current context handler does not permit
			// sub bean validation, then skip this context handler.
			if (!context.getNestedPath().isEmpty() && !contextHandler.enableDuringSubBeanValidation()) {
				continue;
			}
			
			// if the specified context is active
			// initialize the spel resolver, run the rules, then reset the resolver
			if (contextHandler.isActive(validationContext.getQualifiers(), 
					context.getRootModel(), context.getValidationHints())) {
				contextHandler.initializeSPELResolver(context.getSpelResolver());
				callRules(context, validationContext);
				context.getSpelResolver().reset();
			}
		}
	}
	
	/**
	 * Runs the provided rule, and on success either executes the child rules 
	 * (if present) or just continued (if no children present) or on failure
	 * skips the child rules (if present) or logs an error (if no children present).
	 * @param context The current validation context indicating the object being validated
	 * @param rule The validation rule to execute
	 */
	protected void handleValidationRule(ValidationEvaluationContext context, ValidationRule rule) {
		if (passes(rule, context)) {
			// If the rule passes and it has children,
			// it is a condition for nested elements.
			// Call children instead.
			if (rule.hasChildren()) {
				if (log.isDebugEnabled()) {
					log.debug("Running " + rule.getRules().size() + " nested rules.");
				}
				callRules(context, rule);
			}
		} else {
			// If the rule fails and it has children,
			// it is a condition for nested elements.
			// Skip nested elements.
			if (rule.hasChildren() && rule.getOnFail() != RuleErrorMode.ERROR) {
				return;
			} else if (rule.getOnFail() != RuleErrorMode.SKIP_CHILDREN) {
				// If the rule has no children and fails,
				// perform fail action.
				validationErrorMessageHandler.logError(context, rule);
			}
		}
	}
	
	/**
	 * Determines all the indexed collection paths which can be
	 * derived from the current path, which should be passed to
	 * the rule being evaluated, based on the rule's given strategy 
	 * for handling collections.
	 * @param context The current validation context indicating the object being validated
	 * @param rule The rule for which any collections will be handled.
	 * @return a mapping of the deepest possible nested collection path 
	 * to a list of indexed collection path replacements.
	 */
	@SuppressWarnings("rawtypes")
	protected SingletonMap resolveCollectionPathReplacements(ValidationEvaluationContext context, ValidationRule rule) {
		String path = context.localizePath(rule.getPath());
		
		// Do nothing with EL paths.
		if (PathUtils.containsEL(path)) {
			return null;
		}
		
		BeanWrapper rootModelWrapper = new BeanWrapperImpl(context.getRootModel());
		List<String> collectionPaths = new ArrayList<String>();
		List<ValidationRule> brokenDownRules = new ArrayList<ValidationRule>();
		
		// First we need to discover which tokens within the given path are collections, if any.
		// We'll also determine the last collection in the path in order to conditionally apply
		// the user-specified collectionStrategy.
		Class<?>[] pathClasses = 
			PathUtils.getClassesForPathTokens(rootModelWrapper.getWrappedClass(), path, false);
		
		// check for empty path
		if (pathClasses == null) {
			brokenDownRules.add(rule);
			return null;
		}
		
		boolean[] tokenCollection = new boolean[pathClasses.length];
		int lastCollectionIndex = -1;
		for (int i = 0; i < pathClasses.length; i++) {
			tokenCollection[i] = pathClasses[i].isArray() || List.class.isAssignableFrom(pathClasses[i]);
			if (tokenCollection[i]) {
				lastCollectionIndex = i;
			}
		}
		
		// if there's no collections here to replace, stop wasting time and return.
		if (lastCollectionIndex == -1 || 
				(lastCollectionIndex == 0 && rule.getCollectionStrategy() == CollectionStrategy.VALIDATE_COLLECTION_OBJECT)) {
			brokenDownRules.add(rule);
			return null;
		}
		
		// Resolve the base path for the collection(s):
		// This is everything from the beggining of the path
		// to the token of the last collection.
		String[] tokens = path.split("\\.");
		String baseCollectionPath = PathUtils.appendPath(Arrays.copyOfRange(tokens, 0, lastCollectionIndex + 1));
		
		for (int i = 0; i <= lastCollectionIndex; i++) {
			String token = tokens[i];
			// if first pass, add the token as the root path.
			if (i == 0) {
				collectionPaths.add(token);
			} else {
				// otherwise, append the new token to all existing paths.
				List <String> appendedCollectionPaths = new ArrayList<String>();
				for (String collectionPath : collectionPaths) {
					appendedCollectionPaths.add(PathUtils.appendPath(collectionPath, token));
				}
				collectionPaths.clear();
				collectionPaths = appendedCollectionPaths;
			}
			
			// iterate through new appended paths, and break down collection tokens.
			List<String> brokenDownCollectionPaths = new ArrayList<String>();
			Iterator<String> collectionPathIterator = collectionPaths.iterator();
			while (collectionPathIterator.hasNext()) {
				String collectionPath = collectionPathIterator.next();
				Class pathClass = rootModelWrapper.getPropertyType(collectionPath);
				if (pathClass != null && (pathClass.isArray() || List.class.isAssignableFrom(pathClass))) {
					
					// if this is the final collection in the path
					// and the collection strategy is validateCollectionObject,
					// leave the collection as an object reference and continue.
					if (i == lastCollectionIndex
							&& rule.getCollectionStrategy() == CollectionStrategy.VALIDATE_COLLECTION_OBJECT) {
						continue;
					}
					
					// otherwise, treat as iteration: 
					// remove the reference to the collection object and generate sub-paths.
					collectionPathIterator.remove();
					
					Object collectionObject = rootModelWrapper.getPropertyValue(collectionPath);
					// skip this path for sub bean validation if the collection itself is null.
					if (collectionObject == null) {
						continue;
					}
					// Add each index of the collection as a subpath for iteration.
					int collectionSize = pathClass.isArray() ? 
						((Object[]) collectionObject).length : 
						((List) collectionObject).size();
					for (int j = 0; j < collectionSize; j++) {
						brokenDownCollectionPaths.add(collectionPath + "[" + j + "]");
					}
				}
			}
			collectionPaths.addAll(brokenDownCollectionPaths);
		}
		
		return new SingletonMap(baseCollectionPath, collectionPaths);
	}

	/**
	 * Determines if the validation rule passes
	 * by calling the rule executor.
	 * Delegates to extract the model and arguments from 
	 * the sub path defined on the XML rule. 
	 * @param rule The validation rule to run
	 * @param rootModel The model to run the rule on.
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected boolean passes(ValidationRule rule, ValidationEvaluationContext context) {
		if (log.isDebugEnabled()) {
			log.debug("Preparing to execute rule: " + rule);
			log.debug("Actual rule path is " + context.localizePath(rule.getPath()));
		}
		
		// get args
		Object ruleModel = context.resolveRuleModel(rule);
		if (log.isDebugEnabled()) {
			log.debug("Resolved rule model: " + ruleModel);
		}
		
		Object ruleArg = context.resolveRuleArgument(rule);
		if (log.isDebugEnabled()) {
			log.debug("Resolved rule argument: " + ruleModel);
		}

		// call method
		boolean isValid;
		RuleExecutor executor = ruleExecutorContainer.getRuleExecutorByName(rule.getType());
		try {
			// perform conversion on argument
			Object convertedRuleArg = convertRuleArgument(ruleArg, executor);
			isValid = executor.validate(ruleModel, convertedRuleArg);
		} catch (Exception ve) {
			throw new RuntimeException("Error occured during validation: ", ve);
		}
		log.debug("Rule executor returned " + isValid);
		return isValid;
	}
	
	/**
	 * Converts the given validation rule argument into the 
	 * type requested in the validation rule executor's parameterized
	 * argument type 
	 * @param ruleArg the rule argument to convert
	 * @param executor the validation rule executor
	 * @return the converted argument
	 */
	@SuppressWarnings("rawtypes")
	public Object convertRuleArgument(Object ruleArg, RuleExecutor executor) {
		Object convertedRuleArg = ruleArg;
		if (ruleArg != null) {
			Class<?> unwrappedExecutorClass = AopUtils.getTargetClass(executor);
			Class<?>[] parameterizedTypes = GenericTypeResolver.resolveTypeArguments(unwrappedExecutorClass, RuleExecutor.class);
			convertedRuleArg = getTypeConverter().convertIfNecessary(ruleArg, parameterizedTypes[1]);
		}
		return convertedRuleArg;
	}
	
	/**
	 * Return the underlying TypeConverter.
	 */
	protected TypeConverter getTypeConverter() {
		if (this.typeConverter == null) {
			this.typeConverter = new SimpleTypeConverter();
			if (this.conversionService != null) {
				((SimpleTypeConverter) this.typeConverter).setConversionService(this.conversionService);
			}
		}
		return this.typeConverter;
	}

}

