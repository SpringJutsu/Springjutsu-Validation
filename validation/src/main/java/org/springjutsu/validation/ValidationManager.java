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

package org.springjutsu.validation;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.map.SingletonMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.beanvalidation.CustomValidatorBean;
import org.springjutsu.validation.context.ValidationContextHandler;
import org.springjutsu.validation.context.ValidationContextHandlerContainer;
import org.springjutsu.validation.executors.RuleExecutor;
import org.springjutsu.validation.executors.RuleExecutorContainer;
import org.springjutsu.validation.rules.CollectionStrategy;
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
	 * Configurable message code prefix for discovering error messages.
	 */
	private String errorMessagePrefix = "errors";
	
	/**
	 * Configurable message code prefix for discovering field labels. 
	 */
	private String fieldLabelPrefix = null;
	
	/**
	 * Whether or not to enable 
	 */
	private boolean enableSuperclassFieldLabelLookup = true;

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
	 * We'll load error message definitions from
	 * the spring message source.
	 * @see MessageSource
	 */
	@Autowired
	protected MessageSource messageSource;
	
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
		
		for (ValidationContext validationContext : validationEntity.getValidationContexts()) {
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
		 
		// Get fields for subbeans and iterate
		BeanWrapperImpl subBeanWrapper = new BeanWrapperImpl(validateMe);
		PropertyDescriptor[] propertyDescriptors = subBeanWrapper.getPropertyDescriptors();		
		for (PropertyDescriptor property : propertyDescriptors) {
			
			if (!validationEntity.getIncludedPaths().isEmpty() 
					&& !validationEntity.getIncludedPaths().contains(property.getName())) {
				continue;
			}
			
			if (validationEntity.getExcludedPaths().contains(property.getName())) {
				continue;
			}
			
			Class<?> pathClass = PathUtils.getClassForPath(subBeanWrapper.getWrappedClass(), property.getName(), false);
			Class<?> collectionPathClass = PathUtils.getClassForPath(subBeanWrapper.getWrappedClass(), property.getName(), true);
			
			if (rulesContainer.supportsClass(pathClass)) {
				context.pushNestedPath(property.getName());
				doValidate(context);
				context.popNestedPath();
			} else if (rulesContainer.supportsClass(collectionPathClass) && (List.class.isAssignableFrom(pathClass) || pathClass.isArray())) {
				Object potentialList = subBeanWrapper.getPropertyValue(property.getName());
				List<?> list = (List<?>) (pathClass.isArray() && potentialList != null 
					? Arrays.asList(potentialList) : potentialList);
				
				if (list == null || list.isEmpty()) {
					continue;
				}
				
				for (int i = 0; i < list.size(); i++) {
					String nestedPathSegment = property.getName() + "[" + i + "]";
					if (log.isDebugEnabled()) {
						log.debug("Pushing nested path: " + nestedPathSegment);
					}
					
					context.pushNestedPath(nestedPathSegment);
					doValidate(context);
					context.popNestedPath();
					log.debug("Done validating nested path: " + nestedPathSegment);
				}
			}
		}
		if (log.isDebugEnabled()) {
			String nestedPath = PathUtils.joinPathSegments(context.getNestedPath());
			log.debug("Done validating recursion path: " + (nestedPath.isEmpty() ? "root object" : nestedPath));
		}
	}
	
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
	}
	
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
			if (rule.hasChildren()) {
				return;
			} else {
				// If the rule has no children and fails,
				// perform fail action.
				logError(context, rule);
			}
		}
	}
	
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
			isValid = executor.validate(ruleModel, ruleArg);
		} catch (Exception ve) {
			throw new RuntimeException("Error occured during validation: ", ve);
		}
		log.debug("Rule executor returned " + isValid);
		return isValid;
	}
	
	/**
	 * In the event that a validation rule fails, this method is responsible
	 * for recording an error message on the affected path of the Errors object.
	 * The error message is gathered in three parts:
	 * First the base message, if not provided is based on the rule executor class.
	 * This is a message like "\{0} should be longer than \{1} chars."
	 * Next, the first argument \{0} is the model descriptor. This will resolve to a 
	 * label for the path that failed, based on second to last path subBean and the
	 * field that failed. So, if "account.accountOwner.username" had an error, 
	 * it would look for a message based on the class name of accountOwner, and the
	 * field username: like "user.username". If the message files contained a 
	 * "user.username=User name", then the message would now read something like
	 * "User name should be longer than \{1} chars." 
	 * Finally, the argument is resolved. 
	 * If the argument is just a flat string, like "16", then you would get 
	 * "User name should be longer than 16 chars."
	 * If the argument contained EL that resolved on the model, it would perform
	 * the same model lookup detailed above, so you could potentially have something 
	 * like "User name should be longer than First name", which is a bit weird, but
	 * has its uses.
	 * For either the model or argument lookup, if EL is used in the path 
	 * which resolves off the model, the literal value of the evaluated 
	 * EL expression is used.
	 * @param rule the rule which failed
	 * @param rootModel the root model (not failed bean)
	 * @param errors standard Errors object to record error on.
	 */
	protected void logError(ValidationEvaluationContext context, ValidationRule rule) {
		String localizedRulePath = context.localizePath(rule.getPath());
		String errorMessageKey = rule.getMessage();
        if (errorMessageKey == null || errorMessageKey.isEmpty()) {
                errorMessageKey = (errorMessagePrefix != null  && !errorMessagePrefix.isEmpty() ? errorMessagePrefix + "." : "") + rule.getType();
        }
        
		String defaultError =  localizedRulePath + " " + rule.getType();
		String modelMessageKey = getMessageResolver(context, rule, true);
        String ruleArg = getMessageResolver(context, rule, false);
		
		MessageSourceResolvable modelMessageResolvable = 
			new DefaultMessageSourceResolvable(new String[] {modelMessageKey}, modelMessageKey);
		MessageSourceResolvable argumentMessageResolvable = 
			new DefaultMessageSourceResolvable(new String[] {ruleArg}, ruleArg);
		
		// get the local path to error, in case errors object is on nested path.
		String errorMessagePath = rule.getErrorPath();
        if (errorMessagePath != null && !errorMessagePath.isEmpty()) {
        	errorMessagePath = context.localizePath(errorMessagePath);
        } else {
        	errorMessagePath = localizedRulePath;
        }
		
		if (PathUtils.containsEL(errorMessagePath)) {
			throw new IllegalStateException("Could not log error for rule: " + rule.toString() + ". Rules with EL path should specify the errorPath attribute.");
		}

		context.getErrors().rejectValue(errorMessagePath, errorMessageKey, 
				new Object[] {modelMessageResolvable, argumentMessageResolvable}, defaultError);
	}
	
	/**	
	 * This method is responsible for getting the the String used
	 * to resolve the message that should be recorded as the error message.
	 * This proceeds as described in the logError message:
	 * If EL is utilized, and the EL path resolves on the bean, use a string 
	 * like owningClassName.fieldName to resolve a message.
	 * If EL is utilized, and the EL path does not resolve on the bean,
	 * use the literal value of the evaluated EL.
	 * IF EL is not utilized, and we're evaluating for the model, use the 
	 * model field path like owningClassName.fieldName to resolve message.
	 * If EL is not utilized, and we're evaluating for the argument, use the
	 * literal string that's passed in as the argument.
	 * @param model The root model on which the path describes the error location. 
	 * @param rulePath The path which was given to the rule. The path that was 
	 * validated using the rule and failed.
	 * @param resolveAsModel if true, use the behavior to resolve the model. 
	 *  Otherwise, use the behavior to resolve the argument.
	 * @return A string used to look up the message to resolve as the model
	 * or argument of a failed validation rule, as determined by resolveAsModel. 
	 */
	protected String getMessageResolver(ValidationEvaluationContext context, ValidationRule rule, boolean resolveAsModel) {
		String rulePath = resolveAsModel ? rule.getPath() : rule.getValue();
		// if there is no path, return.
		if (rulePath == null || rulePath.length() < 1) {
			return rulePath;
		} else if (PathUtils.isEL(rulePath)) {
			// If the path is actually an expression language statement
			// Need to check if it resolves to a path on the model.
			// trim off EL denotation #{}
			String expressionlessValue = rulePath.substring(2, rulePath.length() - 1);
			// trim off any possible model prefix e.g. model.path.field
			if (expressionlessValue.startsWith("model.")) {
				expressionlessValue = expressionlessValue.substring(6);
			}
			// check if path matches a path on the model.
			if (new BeanWrapperImpl(context.getRootModel()).isReadableProperty(expressionlessValue)) {
				// Since this matched a model path, get the label 
				// for the resolved model.
				return getModelMessageKey(expressionlessValue, context.getRootModel());
			} else {
				// It's not a model object, so we don't need the label message key.
				// Instead, use the value of the expression as a label.
				// If the expression fails, just use the expression itself.
				return String.valueOf(context.resolveRuleModel(rule));
			}
		} else {
			if (resolveAsModel) {
				// not an expression, just get the model message key.
				return getModelMessageKey(context.localizePath(rulePath), context.getRootModel());
			} else {
				// not an expression, return literal
				return rulePath;
			}
		}
	}
	
	/**
	 * If we're trying to resolve the message key for a path on the model,
	 * this method will unwrap that message key.
	 * For instance, consider our model is a Account instance, which has a 
	 * field accountOwner of type User, and that User object has a 
	 * username field of type String:
	 * If rulePath was "accountOwner.username", then it would return a
	 * message key of "user.username", which is the simple classname of the
	 * owning object of the failed validation path, and the field name.
	 * This is so we can display the label of the field that failed validation
	 * in the error message. For instance "User Name must be 8 chars" instead
	 * of something cryptic like "accountOwner.username must be 8 chars".
	 * @param rulePath Validation rule path to the failed field.
	 * @param rootModel The root model owning the field that failed.
	 * @return A message key used to resolve a message describing the field
	 * that failed.
	 */
	protected String getModelMessageKey(String rawRulePath, Object rootModel) {
		
		if (rawRulePath == null || rawRulePath.length() < 1) {
			return rawRulePath;
		}
		
		// clean up any collection and/or map indexing paths from last path segment.
		String rulePath = rawRulePath.trim().replaceAll("\\[[^\\]]+\\]$", "");

		Class<?> parentType = null;
		String fieldPath = null;
		
		if (rulePath.contains(".")) {
			fieldPath = rulePath.substring(rulePath.lastIndexOf(".") + 1);
			String parentPath = rulePath.substring(0, rulePath.lastIndexOf("."));
			BeanWrapperImpl beanWrapper = new BeanWrapperImpl(rootModel);
			parentType = beanWrapper.getPropertyType(parentPath);
		} else {
			fieldPath = rulePath;
			parentType = rootModel.getClass();
		}
		
		if (enableSuperclassFieldLabelLookup) {
			MessageSourceAccessor messageSourceAccessor = new MessageSourceAccessor(messageSource);
			Class<?> messageBearingType = parentType;
			while (messageBearingType != null) {
				if (!messageSourceAccessor.getMessage(
						buildMessageKey(messageBearingType, fieldPath), "MessageNotFound")
						.equals("MessageNotFound")) {
					break;
				}
				else {
					messageBearingType = messageBearingType.getSuperclass();
				}
			}
			if (messageBearingType != null) {
				parentType = messageBearingType;
			}
		}
		
		return buildMessageKey(parentType, fieldPath);
	}
	
	protected String buildMessageKey(Class<?> parentType, String fieldPath) {
		return (fieldLabelPrefix != null && !fieldLabelPrefix.isEmpty() ? fieldLabelPrefix + "." : "")
		+ StringUtils.uncapitalize(parentType.getSimpleName()) + "." + fieldPath;
	}
	
	public String getErrorMessagePrefix() {
		return errorMessagePrefix;
	}

	public void setErrorMessagePrefix(String errorMessagePrefix) {
		this.errorMessagePrefix = errorMessagePrefix == null ? "" : errorMessagePrefix;
	}

	public String getFieldLabelPrefix() {
		return fieldLabelPrefix;
	}

	public void setFieldLabelPrefix(String fieldLabelPrefix) {
		this.fieldLabelPrefix = fieldLabelPrefix == null ? "" : fieldLabelPrefix;
	}
	
	public boolean getEnableSuperclassFieldLabelLookup() {
		return enableSuperclassFieldLabelLookup;
	}

	public void setEnableSuperclassFieldLabelLookup(
			boolean enableSuperclassFieldLabelLookup) {
		this.enableSuperclassFieldLabelLookup = enableSuperclassFieldLabelLookup;
	}
}

