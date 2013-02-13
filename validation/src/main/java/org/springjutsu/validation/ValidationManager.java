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
import org.springframework.webflow.execution.RequestContextHolder;
import org.springjutsu.validation.executors.RuleExecutor;
import org.springjutsu.validation.executors.RuleExecutorContainer;
import org.springjutsu.validation.rules.CollectionStrategy;
import org.springjutsu.validation.rules.ValidationEntity;
import org.springjutsu.validation.rules.ValidationRule;
import org.springjutsu.validation.rules.ValidationRulesContainer;
import org.springjutsu.validation.util.PathUtils;
import org.springjutsu.validation.util.RequestUtils;

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
public class ValidationManager extends CustomValidatorBean  {
	
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
	 * We'll load error message definitions from
	 * the spring message source.
	 * @see MessageSource
	 */
	@Autowired
	protected MessageSource messageSource;
	
	/**
	 * We delegate to rule container executor,
	 * in order to see if rules have been mapped for this
	 * class. If none have, then we don't support it.
	 * @see #RuleExecutorContainer.supportsClass(Class)
	 * @see #javax.validation.Validator.supports(Class)
	 */
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
		Errors errors = new BeanPropertyBindingResult(target, "validationTarget");
		validate(target, errors);
		return errors;
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
		validate(target, errors);
	}
	
	/**
	 * Validation entry point defined in SpringValidatorAdapter
	 */
	@Override
	public void validate(Object model, Errors errors) {
		String currentForm = null;
		if (RequestUtils.getRequest() != null) {
			if (RequestUtils.isWebflowRequest()) {
				currentForm = getWebflowFormName();
			} else {
				currentForm = getMVCFormName();
			}
		}
		validateModel(new ValidationContext(model, errors, currentForm));
	}

	
	protected void validateModel(ValidationContext context) {
		if (context.getModelWrapper() == null) {
			return;
		}
		
		Object validateMe = context.getBeanAtNestedPath();
		
		// Infinite recursion check
		if (validateMe == null || context.previouslyValidated(validateMe)) {
			return;
		} else {
			context.markValidated(validateMe);
		}
		
		List<ValidationRule> rules = rulesContainer.getRules(validateMe.getClass(), context.getCurrentForm());
		callRules(context, rules, false);
		 
		// Get fields for subbeans and iterate
		BeanWrapperImpl subBeanWrapper = new BeanWrapperImpl(validateMe);
		PropertyDescriptor[] propertyDescriptors = subBeanWrapper.getPropertyDescriptors(); 
		for (PropertyDescriptor property : propertyDescriptors) {
			
			ValidationEntity validationEntity = rulesContainer.getValidationEntity(validateMe.getClass());
			
			if (!validationEntity.getIncludedPaths().isEmpty() 
					&& !validationEntity.getIncludedPaths().contains(property.getName())) {
				continue;
			}
			
			if (validationEntity.getExcludedPaths().contains(property.getName())) {
				continue;
			}
			
			if (rulesContainer.supportsClass(property.getPropertyType())) {
				context.pushNestedPath(property.getName());
				validateModel(context);
				context.popNestedPath();
			} else if (List.class.isAssignableFrom(property.getPropertyType()) || property.getPropertyType().isArray()) {
				Object potentialList = subBeanWrapper.getPropertyValue(property.getName());
				List<?> list = (List<?>) (property.getPropertyType().isArray() 
					&& potentialList  != null ? Arrays.asList(potentialList) 
					: potentialList);
				
				if (list == null || list.isEmpty()) {
					continue;
				} else if (list.get(0) == null || !supports(list.get(0).getClass())) {
					continue;
				}
				
				for (int i = 0; i < list.size(); i++) {
					context.pushNestedPath(property.getName() + "[" + i + "]");
					validateModel(context);
					context.popNestedPath();
				}
			}
		}
	}
	
	protected void callRules(ValidationContext context, List<ValidationRule> modelRules, boolean prelocalized) {
		if (modelRules == null) {
			return;
		}
		for (ValidationRule rule : modelRules) {
			
			// skip form rules during sub-bean validation
			if (!context.getNestedPath().isEmpty() && !context.getCurrentForm().isEmpty()) {
				continue;
			}
			
			// adapt rule to current model path.
			ValidationRule localizedRule = PathUtils.containsEL(rule.getPath()) ? rule : prelocalized 
					? rule : rule.cloneWithBasePath(PathUtils.joinPathSegments(context.getNestedPath()));
			
			// break down any collections into indexed paths.
			List<ValidationRule> adaptedRules = considerCollectionPaths(localizedRule, context.getRootModel());
			
			for (ValidationRule adaptedRule : adaptedRules) {
				
				if (passes(adaptedRule, context)) {
					// If the rule passes and it has children,
					// it is a condition for nested elements.
					// Call children instead.
					if (adaptedRule.hasChildren()) {
						callRules(context, adaptedRule.getRules(), true);
					}
				} else {
					// If the rule fails and it has children,
					// it is a condition for nested elements.
					// Skip nested elements.
					if (adaptedRule.hasChildren()) {
						continue;
					} else {
						// If the rule has no children and fails,
						// perform fail action.
						logError(context, adaptedRule);
					}
				}
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	private List<ValidationRule> considerCollectionPaths(ValidationRule rule, Object rootModel) {
		String path = rule.getPath();
		BeanWrapper rootModelWrapper = new BeanWrapperImpl(rootModel);
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
			return brokenDownRules;
		}
		
		boolean[] tokenCollection = new boolean[pathClasses.length];
		int lastCollectionIndex = -1;
		for (int i = 0; i < pathClasses.length; i++) {
			tokenCollection[i] = pathClasses[i].isArray() || List.class.isAssignableFrom(pathClasses[i]);
			if (tokenCollection[i]) {
				lastCollectionIndex = i;
			}
		}
		
		// if there's no collections here to replace, stop wasting time and return single path.
		if (lastCollectionIndex == -1 || 
				(lastCollectionIndex == 0 && rule.getCollectionStrategy() == CollectionStrategy.VALIDATE_COLLECTION_OBJECT)) {
			brokenDownRules.add(rule);
			return brokenDownRules;
		}
		
		// Now split tokens to begin generating broken-down collection paths.
		String[] tokens = path.split("\\.");
		
		for (int i = 0; i < tokens.length; i++) {
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
		
		// Collection paths have been broken down.
		// Now need to make some rules.
		// Using the last collection path token index, 
		// discern the original and adapted collection tokens
		// for each broken down collection path, and apply 
		// replacement recursively through nested rules.
		int lastReplacementIndex = rule.getCollectionStrategy() == 
			CollectionStrategy.VALIDATE_COLLECTION_OBJECT ? lastCollectionIndex - 1 : lastCollectionIndex;
		
		String replacableCollectionSubPath = PathUtils.subPath(path, 0, lastReplacementIndex);
		for (String collectionPath : collectionPaths) {
			String replacementCollectionSubPath = PathUtils.subPath(collectionPath, 0, lastReplacementIndex);
			ValidationRule brokenDownRule = rule.clone();
			brokenDownRule.applyBasePathReplacement(replacableCollectionSubPath, replacementCollectionSubPath);
			brokenDownRules.add(brokenDownRule);
		}
		
		return brokenDownRules;
	}

	/**
	 * Just cleans up a Servlet path URL for rule resolving by
	 * the rules container.
	 * Restful URL paths may be used, with \{variable} path support.
	 * As of 0.6.1, ant paths like * and ** may also be used.
	 */
	protected String getMVCFormName() {
		return RequestUtils.removeLeadingAndTrailingSlashes(
				RequestUtils.getRequest().getServletPath());
	}
	
	/**
	 * Gets a identifier of the current state that needs validating in
	 * order to determine what rules to load from the validation definition.
	 * For webflow, this is the flow ID appended with a colon, and then the 
	 * state id.
	 * For example /accounts/account-creation:basicInformation
	 * @return the context rules associated with this identifier.
	 */
	protected String getWebflowFormName() {
		StringBuffer flowStateId = new StringBuffer();
		flowStateId.append(RequestContextHolder.getRequestContext().getCurrentState().getOwner().getId());
		flowStateId.append(":");
		flowStateId.append(RequestContextHolder.getRequestContext().getCurrentState().getId());
		String flowStateIdString = RequestUtils.removeLeadingAndTrailingSlashes(
				flowStateId.toString());
		return flowStateIdString;
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
	protected boolean passes(ValidationRule rule, ValidationContext context) {
		// get args
		Object ruleModel = context.resolveRuleModel(rule);
		Object ruleArg = context.resolveRuleArgument(rule);

		// call method
		boolean isValid;
		RuleExecutor executor = ruleExecutorContainer.getRuleExecutorByName(rule.getType());
		try {
			isValid = executor.validate(ruleModel, ruleArg);
		} catch (Exception ve) {
			throw new RuntimeException("Error occured during validation: ", ve);
		}
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
	protected void logError(ValidationContext context, ValidationRule rule) {
		String errorMessageKey = rule.getMessage();
        if (errorMessageKey == null || errorMessageKey.isEmpty()) {
                errorMessageKey = (errorMessagePrefix != null  && !errorMessagePrefix.isEmpty() ? errorMessagePrefix + "." : "") + rule.getType();
        }
        
		String defaultError =  rule.getPath() + " " + rule.getType();
		String modelMessageKey = getMessageResolver(context, rule, true);
        String ruleArg = getMessageResolver(context, rule, false);
		
		MessageSourceResolvable modelMessageResolvable = 
			new DefaultMessageSourceResolvable(new String[] {modelMessageKey}, modelMessageKey);
		MessageSourceResolvable argumentMessageResolvable = 
			new DefaultMessageSourceResolvable(new String[] {ruleArg}, ruleArg);
		
		// get the local path to error, in case errors object is on nested path.
		String errorMessagePath = rule.getErrorPath();
        if (errorMessagePath == null || errorMessagePath.isEmpty()) {
                errorMessagePath = rule.getPath();
        }
		if (!context.getErrors().getNestedPath().isEmpty() && errorMessagePath.startsWith(context.getErrors().getNestedPath())) {
			errorMessagePath = PathUtils.appendPath(errorMessagePath.substring(context.getErrors().getNestedPath().length()), "");
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
	protected String getMessageResolver(ValidationContext context, ValidationRule rule, boolean resolveAsModel) {
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
				return getModelMessageKey(rulePath, context.getRootModel());
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

