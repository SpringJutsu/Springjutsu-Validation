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

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.context.support.MessageSourceAccessor;
import org.springjutsu.validation.rules.ValidationRule;
import org.springjutsu.validation.util.PathUtils;

/**
 * Responsible for handling the resolution
 * of and logging of error messages against
 * an Errors object 
 * @author Clark Duplichien
 *
 */
public class ValidationErrorMessageHandler {
	
	/**
	 * Configurable message code prefix for discovering error messages.
	 */
	private String errorMessagePrefix = "errors";
	
	/**
	 * Configurable message code prefix for discovering field labels. 
	 */
	private String fieldLabelPrefix = null;
	
	/**
	 * Whether or not to attempt to use the super class' class name
	 * as part of a field label when looking up field labels for a child 
	 * class when no label is found for the child class name and field name.
	 */
	private boolean enableSuperclassFieldLabelLookup = true;
	
	/**
	 * We'll load error message definitions from
	 * the spring message source.
	 * @see MessageSource
	 */
	@Autowired
	protected MessageSource messageSource;
	
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
        
		String defaultError = rule.getMessage() != null && !rule.getMessage().isEmpty() ? rule.getMessage() : localizedRulePath + " " + rule.getType();
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
	
	/**
	 * Constructs the actual model message key for a field label.
	 * @param parentType the class of the bean on which the field path is found
	 * @param fieldPath the path of the field for which a message is being resolved
	 * @return the constructed message key, prefixed with a configured field label prefix
	 */
	protected String buildMessageKey(Class<?> parentType, String fieldPath) {
		return (fieldLabelPrefix != null && !fieldLabelPrefix.isEmpty() ? fieldLabelPrefix + "." : "")
		+ StringUtils.uncapitalize(parentType.getSimpleName()) + "." + fieldPath;
	}
	
	/**
	 * @return The configured error message prefix
	 */
	public String getErrorMessagePrefix() {
		return errorMessagePrefix;
	}

	/**
	 * Sets the error message prefix.
	 * Sets empty string if passed null.
	 * @param errorMessagePrefix The message prefix for resolved error messages
	 */
	public void setErrorMessagePrefix(String errorMessagePrefix) {
		this.errorMessagePrefix = errorMessagePrefix == null ? "" : errorMessagePrefix;
	}

	/**
	 * @return the configured field label prefix
	 */
	public String getFieldLabelPrefix() {
		return fieldLabelPrefix;
	}

	/**
	 * Sets the field label prefix.
	 * Sets empty string if passed null
	 * @param fieldLabelPrefix The message prefix for resolved field labels
	 */
	public void setFieldLabelPrefix(String fieldLabelPrefix) {
		this.fieldLabelPrefix = fieldLabelPrefix == null ? "" : fieldLabelPrefix;
	}
	
	/**
	 * @return a boolean indicating whether or not super classes are searched
	 * for a field on which an error occurs in the generation of field label message codes.
	 */
	public boolean getEnableSuperclassFieldLabelLookup() {
		return enableSuperclassFieldLabelLookup;
	}

	/**
	 * Sets whether or not super classes are searched
	 * for a field on which an error occurs in the generation of field label message codes.
	 * @param enableSuperclassFieldLabelLookup the boolean value to set
	 */
	public void setEnableSuperclassFieldLabelLookup(
			boolean enableSuperclassFieldLabelLookup) {
		this.enableSuperclassFieldLabelLookup = enableSuperclassFieldLabelLookup;
	}

}
