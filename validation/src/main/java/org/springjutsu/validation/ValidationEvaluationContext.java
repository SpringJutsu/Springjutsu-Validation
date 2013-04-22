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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.validation.Errors;
import org.springjutsu.validation.exceptions.CircularValidationTemplateReferenceException;
import org.springjutsu.validation.exceptions.IllegalTemplateReferenceException;
import org.springjutsu.validation.rules.ValidationRule;
import org.springjutsu.validation.rules.ValidationTemplate;
import org.springjutsu.validation.rules.ValidationTemplateReference;
import org.springjutsu.validation.spel.CurrentModelPropertyAccessor;
import org.springjutsu.validation.spel.SPELResolver;
import org.springjutsu.validation.util.PathUtils;

/**
 * The ValidationEvaluationContext is responsible for tracking
 * contextual information about "where" the validation process is
 * in terms of the rules being evaluated, and nested paths during
 * model bean traversal.
 * It provides access to model objects relative to the current
 * paths being validated, and also is supplied with the current 
 * SPEL resolver initialized for any current validation context(s).
 * Also provides details of the current paths being evaluated, 
 * for purposes of logging and error message resolution. 
 * @author Clark Duplichien
 *
 */
public class ValidationEvaluationContext {
	
	/**
	 * The errors object on which validation errors will be recorded.
	 */
	private Errors errors;
	
	/**
	 * A bean wrapper wrapping the base model object under validation.
	 */
	private BeanWrapper modelWrapper;
	
	/**
	 * The SPEL resolver, with named contexts initialized by 
	 * any active ValidationContextHandler instances. 
	 */
	private SPELResolver spelResolver;
	
	/**
	 * The deep nested path to the current model being evaluated,
	 * rooted from the base model object under evaluation
	 */
	private Stack<String> nestedPath;
	
	/**
	 * A stack of active validation templates in the order they
	 * were activated. Also used to detect infinite recursion.
	 */
	private Stack<String> templateNames;
	
	/**
	 * A nested path from the base model object including all
	 * path segments provided by validation template base paths.
	 */
	private Stack<String> templateBasePaths;
	
	/**
	 * Maps path segments indicating collections to 
	 * path segment replacements indicating the current collection
	 * index being evaluated. In other words, keeps track of the current
	 * position within nested collections. 
	 */
	private Map<String, String> collectionPathReplacements;
	
	/**
	 * The active JSR-303 validation groups. 
	 * Spring calls them validation hints.
	 * Sounds more mysterious.
	 * It's like we're looking for clues, man.
	 */
	private String[] validationHints;
	
	/**
	 * Checked model hashes prevent infinite recursion.
	 * As the recursion stack grows, each list of checked model hashes
	 * is inherited from the prior state in the stack.
	 * This allows the same bean to be validated on different nested 
	 * path structures, but will prevent infinite recursive validation
	 * of the same bean on the same nested path structure in the event
	 * of a cyclic datamodel, as bi-direction relationships are not 
	 * uncommon within JPA bean models. 
	 * This collection must still handle hashes when not recursed, and
	 * as such will always be one push deeper than the nested path stack.
	 */
	private Stack<List<Integer>> checkedModelHashes;
	
	/**
	 * Constructs a new ValidationEvaluationContext.
	 * There will be a single ValidationEvalautionContext created
	 * for each model validation performed.  
	 * @param model The object to validate
	 * @param errors The errors object on which to record errors
	 * @param validationHints Any JSR-303 validation groups to activate
	 */
	public ValidationEvaluationContext(Object model, Errors errors, Object... validationHints) {
		this.modelWrapper = model == null ? null : new BeanWrapperImpl(model);
		this.errors = errors;
		this.validationHints = new String[validationHints.length];
		for (int i = 0; i < validationHints.length; i++) {
			this.validationHints[i] = 
				(validationHints[i] instanceof Class<?>) ? 
					((Class<?>) validationHints[i]).getCanonicalName() :
						String.valueOf(validationHints[i]);
		}
		this.spelResolver = new SPELResolver(model);
		this.spelResolver.getScopedContext().addPropertyAccessor(new CurrentModelPropertyAccessor());
		this.spelResolver.getScopedContext().addContext("currentModel", this.new CurrentModelAccessor());
		this.nestedPath = new Stack<String>();
		this.checkedModelHashes = new Stack<List<Integer>>();
		this.checkedModelHashes.push(new ArrayList<Integer>());
		this.templateNames = new Stack<String>();
		this.templateBasePaths = new Stack<String>();
		this.collectionPathReplacements = new LinkedHashMap<String, String>();
	}
	
	/**
	 * @return the object described by the current nested path,
	 * which is built during recursive model validation.
	 * This does not include any active template base paths.
	 */
	public Object getBeanAtNestedPath() {
		String joinedPath = PathUtils.joinPathSegments(nestedPath);
		return joinedPath.isEmpty() ? getRootModel() : modelWrapper.getPropertyValue(joinedPath);
	}
	
	/**
	 * @param bean The bean to check for repeated validation
	 * @return true if the given bean has already been validated.
	 */
	protected boolean previouslyValidated(Object bean) {
		return checkedModelHashes.peek().contains(bean.hashCode());
	}
	
	/**
	 * Marks the given bean as having already been validated,
	 * to avoid infinite recursion during recursive sub bean validation.
	 * @param bean the bean to mark validated
	 */
	protected void markValidated(Object bean) {
		checkedModelHashes.peek().add(bean.hashCode());
	}
	
	/**
	 * @return the base bean being validated
	 */
	public Object getRootModel() {
		return modelWrapper == null ? null : modelWrapper.getWrappedInstance();
	}
	
	/**
	 * @return the object currently under evaluation based
	 * on any nested and/or template paths. 
	 */
	public Object getCurrentModel() {
		String currentPath = getCurrentNestedPath();
		return currentPath.isEmpty() ? getRootModel() : modelWrapper.getPropertyValue(currentPath);
	}
	
	/**
	 * Responsible for discovering the path-described model which
	 * is to be validated by the current rule. This path may contain
	 * EL, and if it does, we delegate to @link(#resolveEL(String))
	 * to resolve that EL.
	 * @param rule The rule for which to resolve the model
	 * @return the resolved rule model
	 */
	protected Object resolveRuleModel(ValidationRule rule) {
		Object result = null;
		if (rule.getPath() == null || rule.getPath().isEmpty()) {
			return getRootModel();
		}
		// TODO / Note to self: the expression is actually the rule path,
		// which at this point has already been localized by the nested path
		// via rule cloning, so long as the rule path didn't contain EL
		if (PathUtils.containsEL(rule.getPath())) {
			result = spelResolver.resolveSPELString(rule.getPath());
		} else {
			BeanWrapperImpl beanWrapper = new BeanWrapperImpl(getRootModel());
			String localizedRulePath = localizePath(rule.getPath());
			// TODO: Why is this check here?
			// Under what circumstances did we want this to return null
			// instead of throwing an exception?
			if (beanWrapper.isReadableProperty(localizedRulePath)) {
				result = beanWrapper.getPropertyValue(localizedRulePath);
			}
		}
		return result;
	}
	
	/**
	 * Responsible for determining the argument to be passed to the rule.
	 * If the argument expression string contains EL, it will be resolved,
	 * otherwise, the expression string is taken as a literal argument.
	 * @param rule the rule for which to resolve argument
	 * @param expression The string path expression for the model.
	 * @return the Object to serve as a rule argument
	 */
	protected Object resolveRuleArgument(ValidationRule rule) {
		Object result = null;
		if (rule.getValue() == null || rule.getValue().isEmpty()) {
			return null;
		}
		if (PathUtils.containsEL(rule.getValue())) {
			result = spelResolver.resolveSPELString(rule.getValue());
		} else {
			result = rule.getValue();
		}
		return result;
	}
	
	/**
	 * Used during recursive sub-bean validation to indicate
	 * that the validation process is moving to a sub bean path.
	 * @param subPath the field name of a sub bean which will be validated next
	 * @return the object at the pushed nested path.
	 */
	protected Object pushNestedPath(String subPath) {
		nestedPath.push(subPath);
		checkedModelHashes.push(new ArrayList<Integer>(checkedModelHashes.peek()));
		return getBeanAtNestedPath();
	}
	
	/**
	 * Called after validating all fields on a sub-bean, this method
	 * removes the sub bean's field and returns the validation context
	 * to its owning object.
	 */
	protected void popNestedPath() {
		nestedPath.pop();
		checkedModelHashes.pop();
	}
	
	/**
	 * Pushes a validation template onto the validation template stack,
	 * after ensuring that the given validation template is not already active
	 * in order to prevent infinite recursion of nested validation templates
	 * @param templateReference the validation template reference object which indicates
	 * what nested path the validation template applies to
	 * @param actualTemplate the validation template referenced by the validation template reference
	 */
	protected void pushTemplate(ValidationTemplateReference templateReference, ValidationTemplate actualTemplate) {
		if (templateNames.contains(templateReference.getTemplateName())) {
			throw new CircularValidationTemplateReferenceException(
				"Circular use of validation template named " + templateReference.getTemplateName());
		}
		String localizedTemplatePath = localizePath(templateReference.getBasePath());
		Class<?> templateTargetClass = PathUtils.getClassForPath(modelWrapper.getWrappedClass(), localizedTemplatePath, true);
		if (!actualTemplate.getApplicableEntityClass().isAssignableFrom(templateTargetClass)) {
			throw new IllegalTemplateReferenceException(
				"Template named " + actualTemplate.getName() + 
				" expects class " + actualTemplate.getApplicableEntityClass() +
				" but got instance of " + templateTargetClass);
		}
		
				
		templateNames.push(templateReference.getTemplateName());
		templateBasePaths.push(templateReference.getBasePath());
	}
	
	/**
	 * Removes the last validation template reference from 
	 * the validation template and template reference stacks.
	 */
	protected void popTemplate() {
		templateNames.pop();
		templateBasePaths.pop();
	}
	
	/**
	 * Performs the following operations to localize a sub path
	 * (e.g. rule path) to the current context:
	 * 1) prepends with template base paths
	 * 2) prepends resultant path with nestedPath
	 * 3) applies collection replacements 
	 * @param subPath the path to localize
	 * @return currently localizedPath
	 */
	protected String localizePath(String subPath) {
		if (PathUtils.containsEL(subPath)) {
			return subPath;
		}
		String localizedPath = PathUtils.appendPath(
				PathUtils.joinPathSegments(nestedPath),
				PathUtils.joinPathSegments(templateBasePaths), 
				subPath);
		// Apply collection path replacements.
		// Multiple collection paths may build off of one another,
		// so it is important to run all possible path replacements.
		// Path replacement order is maintained by the use of a LinkedHashMap
		for (Map.Entry<String, String> collectionPathReplacement : collectionPathReplacements.entrySet()) {
			if (localizedPath.startsWith(collectionPathReplacement.getKey())) {
				localizedPath = localizedPath.replaceAll(
					"^" + Pattern.quote(collectionPathReplacement.getKey()),
					collectionPathReplacement.getValue());
			}
		}
		return localizedPath;
	}
	
	/**
	 * @return the current nested path including any 
	 * pushed nested paths from recursive sub bean validation,
	 * any active validation template reference base paths,
	 * and any collection path replacements from collection iteration.
	 */
	public String getCurrentNestedPath() {
		return localizePath("");
	}
	
	/**
	 * @return the string representations of the qualifiers
	 * for any active JSR-303 validation groups
	 */
	public String[] getValidationHints() {
		return validationHints;
	}

	/**
	 * @return the Errors object on which validation errors
	 * will be recorded
	 */
	public Errors getErrors() {
		return errors;
	}

	/**
	 * @return the bean wrapper wrapping the base bean
	 * under validation
	 */
	public BeanWrapper getModelWrapper() {
		return modelWrapper;
	}

	/**
	 * @return the current SPELResolver initialized
	 * by any active ValidationContextHandler instances
	 */
	public SPELResolver getSpelResolver() {
		return spelResolver;
	}

	/**
	 * @return the current stack of nested paths pushed
	 * by recursive sub bean validation.
	 */
	protected Stack<String> getNestedPath() {
		return nestedPath;
	}	

	/**
	 * @return the hash codes of model beans already validated
	 */
	protected Stack<List<Integer>> getCheckedModelHashes() {
		return checkedModelHashes;
	}
	
	/**
	 * @return the names of active validation templates
	 */
	protected Stack<String> getTemplateNames() {
		return templateNames;
	}

	/**
	 * @return the nested stack of active validation template base paths
	 */
	protected Stack<String> getTemplateBasePaths() {
		return templateBasePaths;
	}
	
	/**
	 * @return the collection path replacements indicating for each 
	 * nested collection path the indexed collection path for the current
	 * iteration of the validated collection.
	 */
	protected Map<String, String> getCollectionPathReplacements() {
		return collectionPathReplacements;
	}
	
	/**
	 * Used by @see CurrentModelPropertyAccessor to gain access
	 * to the current model under validation, without exposing
	 * the entirety of the current ValidationEvaluationContext
	 * to SPEL expressions. 
	 * @author Clark Duplichien
	 *
	 */
	public class CurrentModelAccessor {
		
		public Object accessCurrentModel() {
			return getCurrentModel();
		}
		
	}

}
