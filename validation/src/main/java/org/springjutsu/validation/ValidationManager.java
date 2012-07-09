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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.springframework.core.NamedThreadLocal;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.beanvalidation.CustomValidatorBean;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springjutsu.validation.executors.RuleExecutor;
import org.springjutsu.validation.executors.RuleExecutorContainer;
import org.springjutsu.validation.rules.ValidationRule;
import org.springjutsu.validation.rules.ValidationRulesContainer;
import org.springjutsu.validation.spel.WebContextSPELResolver;
import org.springjutsu.validation.util.RequestUtils;

/**
 * Registerable as a JSR-303 @link{CustomValidatorBean}, this 
 * ValidationManager class is instead responsible for reading
 * XML-driven nested validation rules.
 * However, it populates a standard Errors object as expected.  
 * Logic is divided into two main portions:
 * First context rules are read from the &lt;context-rules>
 * defined for a given path. These rules are loaded based on 
 * the current path the user is accessing, with implementation
 * handled by subclasses.
 * Context rules are typically those rules which are specific 
 * to a given form: the fields which are required, and also
 * conditional validation logic based on other fields or 
 * variables defined in EL or otherwise. These are the 
 * conditional per-form validation rules not handled by JSR-303
 * Second, model rules are read from the &lt;model-rules>
 * defined for a given class. These rules are loaded directly
 * from the per-class definitions provided in the XML rules.
 * Model rules are those rules which do not change for a 
 * given class and include type checking, length checks, 
 * and so on. These are the more typical JSR-303 type rules. 
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
	 * Use one per request to evaluate SPEL Expressions, 
	 * as creation is somewhat expensive. 
	 */
	private static final ThreadLocal<WebContextSPELResolver> spelResolver = 
		new NamedThreadLocal<WebContextSPELResolver>("Validation SPEL Resolver");
	
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
	 * We perform actual validation in the order
	 * of context rules followed by model rules.
	 */
	@Override
	public void validate(Object model, Errors errors) {
		spelResolver.set(new WebContextSPELResolver(model));
		try {
			String currentForm = null;
			if (RequestUtils.getRequest() != null) {
				if (RequestUtils.isWebflowRequest()) {
					currentForm = getWebflowFormName();
				} else {
					currentForm = getMVCFormName();
				}
			}
			validateModel(model, errors, new ArrayList<Object>(), currentForm);
		} finally {
			spelResolver.set(null);
		}
	}
	
	@Override
	public void validate(Object target, Errors errors,
			Object... validationHints) {
		// TODO Support Validation Hints
			validate(target, errors);
	}

	/**
	 * Responsible for testing all XML-defined per-class rules.
	 * We will check recursively: using a BeanWrapper to get a 
	 * @link(PropertyDescriptor) for each field, and then checking to
	 * see if any of the fields are supported by validation rules.
	 * If so, we will test those nested paths using that class's
	 * model rules as well. This ensures that sub beans are properly
	 * validated using their standard model rules.
	 * @param model the model object to validate. May be a recursed sub bean.
	 * @param errors standard Errors object to record validation errors to.
	 * @param checkedModels A list of model objects we have already validated,
	 * 	in order to prevent unneeded or infinite recursion
	 */
	protected void validateModel(Object model, Errors errors, List<Object> checkedModels, String currentForm) {
		if (model == null) {
			return;
		}
		
		BeanWrapperImpl beanWrapper = new BeanWrapperImpl(model);
		Object validateMe = null;
		String beanPath = appendPath(errors.getNestedPath(), "");
		
		// get sub bean to validate
		if (beanPath.isEmpty()) {
			validateMe = model;
		} else {
			validateMe = beanWrapper.getPropertyValue(beanPath);
		}
		
		// Infinite recursion check
		if (validateMe == null || checkedModels.contains(validateMe.hashCode())) {
			return;
		} else {
			checkedModels.add(validateMe.hashCode());
		}
		
		List<ValidationRule> rules = rulesContainer.getRules(validateMe.getClass(), currentForm);
		callRules(model, errors, rules);
		 
		// Get fields for subbeans and iterate
		BeanWrapperImpl subBeanWrapper = new BeanWrapperImpl(validateMe);
		PropertyDescriptor[] propertyDescriptors = subBeanWrapper.getPropertyDescriptors(); 
		for (PropertyDescriptor property : propertyDescriptors) {
			if (rulesContainer.supportsClass(property.getPropertyType())) {
				errors.pushNestedPath(property.getName());
				validateModel(model, errors, inheritedCheckedModels(checkedModels), currentForm);
				errors.popNestedPath();
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
					errors.pushNestedPath(property.getName() + "[" + i + "]");
					validateModel(model, errors, inheritedCheckedModels(checkedModels), currentForm);
					errors.popNestedPath();
				}
			}
		}
	}
	
	/**
	 * Responsible for delegating each actual model rule
	 *  to the appropriate @link{RuleExecutor}.
	 *  Errors are recorded if no previous error has been
	 *  recorded for the given path.
	 * @param model The object being validated
	 * @param errors Standard errors object to record validation errors.
	 * @param modelRules A list of ValidationRules parsed from
	 *  the &lt;model-rules> section of the validation XML.
	 */
	protected void callRules(Object model, Errors errors, List<ValidationRule> modelRules) {
		if (modelRules == null) {
			return;
		}
		for (ValidationRule rule : modelRules) {
			
			// get path to current model
			String appendedPath = appendPath(errors.getNestedPath(), rule.getPath());
			
			// break down any collections into indexed paths.
			List<String> fullPaths = considerCollectionPaths(appendedPath, model);
			
			for (String fullPath : fullPaths) {
			
				// update rule for full path
				ValidationRule modelRule = rule.cloneWithPath(fullPath);
				
				if (passes(modelRule, model)) {
					// If the rule passes and it has children,
					// it is a condition for nested elements.
					// Call children instead.
					if (modelRule.hasChildren()) {
						callRules(model, errors, modelRule.getRules());
					}
				} else {
					// If the rule fails and it has children,
					// it is a condition for nested elements.
					// Skip nested elements.
					if (modelRule.hasChildren()) {
						continue;
					} else {
						// If the rule has no children and fails,
						// perform fail action.
						logError(modelRule, model, errors);
					}
				}
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	private List<String> considerCollectionPaths(String path, Object rootModel) {
		BeanWrapper rootModelWrapper = new BeanWrapperImpl(rootModel);
		List<String> collectionPaths = new ArrayList<String>();
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
					appendedCollectionPaths.add(appendPath(collectionPath, token));
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
					int collectionSize = pathClass.isArray() ? 
						((Object[]) rootModelWrapper.getPropertyValue(collectionPath)).length : 
						((List) rootModelWrapper.getPropertyValue(collectionPath)).size();
					for (int j = 0; j < collectionSize; j++) {
						brokenDownCollectionPaths.add(collectionPath + "[" + j + "]");
					}
					collectionPathIterator.remove();
				}
			}
			collectionPaths.addAll(brokenDownCollectionPaths);
		}
		return collectionPaths;
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
	protected boolean passes(ValidationRule rule, Object rootModel) {			
		// get args
		Object ruleModel = getContextModel(rootModel, rule.getPath());
		Object ruleArg = getContextArgument(rootModel, rule.getValue());

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
	 * Responsible for discovering the path-described model which
	 * is to be validated by the current rule. This path may contain
	 * EL, and if it does, we delegate to @link(#resolveEL(String, Object))
	 * to resolve that EL.
	 * @param model Object to be validated
	 * @param expression The string path expression for the model.
	 * @return the Object to validate.
	 */
	protected Object getContextModel(Object model, String expression) {
		Object result = null;
		if (expression == null || expression.isEmpty()) {
			return model;
		}
		if (hasEL(expression)) {
			result = resolveSPEL(expression, model);
		} else {
			BeanWrapperImpl beanWrapper = new BeanWrapperImpl(model);
			if (model != null && beanWrapper.isReadableProperty(expression)) {
				result = beanWrapper.getPropertyValue(expression);
			}
		}
		return result;
	}
	
	/**
	 * Responsible for determining the argument to be passed to the rule.
	 * If the argument expression string contains EL, it will be resolved,
	 * otherwise, the expression string is taken as a literal argument.
	 * @param model Object to be validated
	 * @param expression The string path expression for the model.
	 * @return the Object to serve as a rule argument
	 */
	protected Object getContextArgument(Object model, String expression) {
		Object result = null;
		if (expression == null || expression.isEmpty()) {
			return null;
		}
		if (hasEL(expression)) {
			result = resolveSPEL(expression, model);
		} else {
			result = expression;
		}
		return result;
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
	protected void logError(ValidationRule rule, Object rootModel, Errors errors) {
		String errorMessageKey = rule.getMessage();
        if (errorMessageKey == null || errorMessageKey.isEmpty()) {
                errorMessageKey = (errorMessagePrefix != null  && !errorMessagePrefix.isEmpty() ? errorMessagePrefix + "." : "") + rule.getType();
        }
        
		String defaultError =  rule.getPath() + " " + rule.getType();
		String modelMessageKey = getMessageResolver(rootModel, rule.getPath(), true);
        String ruleArg = getMessageResolver(rootModel, rule.getValue(), false);
		
		MessageSourceResolvable modelMessageResolvable = 
			new DefaultMessageSourceResolvable(new String[] {modelMessageKey}, modelMessageKey);
		MessageSourceResolvable argumentMessageResolvable = 
			new DefaultMessageSourceResolvable(new String[] {ruleArg}, ruleArg);
		
		// get the local path to error, in case errors object is on nested path.
		String errorMessagePath = rule.getErrorPath();
        if (errorMessagePath == null || errorMessagePath.isEmpty()) {
                errorMessagePath = rule.getPath();
        }
		if (!errors.getNestedPath().isEmpty() && errorMessagePath.startsWith(errors.getNestedPath())) {
			errorMessagePath = appendPath(errorMessagePath.substring(errors.getNestedPath().length()), "");
		}

		errors.rejectValue(errorMessagePath, errorMessageKey, 
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
	protected String getMessageResolver(Object model, String rulePath, boolean resolveAsModel) {
		// if there is no path, return.
		if (rulePath == null || rulePath.length() < 1) {
			return rulePath;
		} else if (hasEL(rulePath)) {
			// If the path is actually an expression language statement
			// Need to check if it resolves to a path on the model.
			// trim off EL denotation #{}
			String expressionlessValue = rulePath.substring(2, rulePath.length() - 1);
			// trim off any possible model prefix e.g. model.path.field
			if (expressionlessValue.startsWith("model.")) {
				expressionlessValue = expressionlessValue.substring(6);
			}
			// check if path matches a path on the model.
			if (new BeanWrapperImpl(model).isReadableProperty(expressionlessValue)) {
				// Since this matched a model path, get the label 
				// for the resolved model.
				return getModelMessageKey(expressionlessValue, model);
			} else {
				// It's not a model object, so we don't need the label message key.
				// Instead, use the value of the expression as a label.
				// If the expression fails, just use the expression itself.
				return String.valueOf(getContextArgument(model, rulePath));
			}
		} else {
			if (resolveAsModel) {
				// not an expression, just get the model message key.
				return getModelMessageKey(rulePath, model);
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
	protected String getModelMessageKey(String rulePath, Object rootModel) {
		
		if (rulePath == null || rulePath.length() < 1) {
			return rulePath;
		}		

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
	
	/**
	 * @param expression A string expression
	 * @return returns true if the expression string is EL.
	 */
	protected boolean hasEL(String expression) {
		return expression.matches(".*\\$\\{.+\\}.*");
	}
	
	/**
	 * Responsible for resolving a SPEL expression.
	 * Unwraps the EL string, creates an instance of a 
	 * @link{SPELReadyRequestContext}, adds all the needed
	 * property accessors, and runs the SPEL evaluation.
	 * TODO: find a better way to return null if not found on any scope.   
	 *  
	 * @param el The EL expression to resolve.
	 * @param model The model on which the EL-described field MAY lie.
	 * @return The object described by the EL expression.
	 */
	protected Object resolveSPEL(String elContaining, Object model) {
		// if the whole thing is a single EL string, try to get the object.
		if (elContaining.matches("\\$\\{(.(?!\\$\\{))+\\}")) {
			String resolvableElString = 
				elContaining.substring(2, elContaining.length() - 1) + "?: null";
			Object elResult = spelResolver.get().getBySpel(resolvableElString);
			return elResult;
		} else {
			// otherwise, do string value substitution to build a value.
			String elResolvable = elContaining;
			Matcher matcher = Pattern.compile("\\$\\{(.(?!\\$\\{))+\\}").matcher(elResolvable);
			while (matcher.find()) {
				String elString = matcher.group();
				String resolvableElString = elString.substring(2, elString.length() - 1) + "?: null";
				Object elResult = spelResolver.get().getBySpel(resolvableElString);
				String resolvedElString = elResult != null ? String.valueOf(elResult) : "";
				elResolvable = elResolvable.replace(elString, resolvedElString);
				matcher.reset(elResolvable);
			}
			return elResolvable;
		}
	}
	
	/**
	 * Appends two subpath segments together and handles
	 * period replacement appropriately.
	 * @param path A string path.
	 * @param suffix A string path to add to the prior path.
	 * @return A combined path.
	 */
	protected String appendPath(String path, String suffix) {
		String newPath = path + (path.endsWith(".") ? "" : ".") + suffix;
		if (newPath.startsWith(".")) {
			newPath = newPath.substring(1);
		}
		if (newPath.endsWith(".")) {
			newPath = newPath.substring(0, newPath.length() - 1);
		}
		return newPath;
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

	protected List<Object> inheritedCheckedModels(List<Object> checkedModels) {
		List<Object> inheritedCheckedModels = new ArrayList<Object>();
		inheritedCheckedModels.addAll(checkedModels);
		return inheritedCheckedModels;
	}	
}

