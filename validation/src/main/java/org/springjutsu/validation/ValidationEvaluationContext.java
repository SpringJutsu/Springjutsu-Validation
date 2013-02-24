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
import org.springjutsu.validation.spel.SPELResolver;
import org.springjutsu.validation.util.PathUtils;

public class ValidationEvaluationContext {
	
	private Errors errors;
	private BeanWrapper modelWrapper;
	private SPELResolver spelResolver;
	private Stack<String> nestedPath;
	private Stack<String> templateNames;
	private Stack<String> templateBasePaths;
	private Map<String, String> collectionPathReplacements;
	
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
	
	public ValidationEvaluationContext(Object model, Errors errors) {
		this.errors = errors;
		this.modelWrapper = model == null ? null : new BeanWrapperImpl(model);
		this.spelResolver = new SPELResolver(model);
		this.nestedPath = new Stack<String>();
		this.checkedModelHashes = new Stack<List<Integer>>();
		this.checkedModelHashes.push(new ArrayList<Integer>());
		this.templateNames = new Stack<String>();
		this.templateBasePaths = new Stack<String>();
		this.collectionPathReplacements = new LinkedHashMap<String, String>(); 
	}
	
	public Object getBeanAtNestedPath() {
		String joinedPath = PathUtils.joinPathSegments(nestedPath);
		return joinedPath.isEmpty() ? getRootModel() : modelWrapper.getPropertyValue(joinedPath);
	}
	
	public boolean previouslyValidated(Object bean) {
		return checkedModelHashes.peek().contains(bean.hashCode());
	}
	
	public void markValidated(Object bean) {
		checkedModelHashes.peek().add(bean.hashCode());
	}
	
	public Object getRootModel() {
		return modelWrapper == null ? null : modelWrapper.getWrappedInstance();
	}
	
	/**
	 * Responsible for discovering the path-described model which
	 * is to be validated by the current rule. This path may contain
	 * EL, and if it does, we delegate to @link(#resolveEL(String))
	 * to resolve that EL.
	 * @param rule The rule for which to resolve the model
	 * @return the resolved rule model
	 */
	public Object resolveRuleModel(ValidationRule rule) {
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
	public Object resolveRuleArgument(ValidationRule rule) {
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
	
	public Object pushNestedPath(String subPath) {
		nestedPath.push(subPath);
		checkedModelHashes.push(new ArrayList<Integer>(checkedModelHashes.peek()));
		return getBeanAtNestedPath();
	}
	
	public void popNestedPath() {
		nestedPath.pop();
		checkedModelHashes.pop();
	}
	
	public void pushTemplate(ValidationTemplateReference templateReference, ValidationTemplate actualTemplate) {
		if (templateNames.contains(templateReference.getTemplateName())) {
			throw new CircularValidationTemplateReferenceException(
				"Circular use of validation template named " + templateReference.getTemplateName());
		}
		String localizedTemplatePath = localizePath(templateReference.getBasePath());
		Class templateTargetClass = PathUtils.getClassForPath(modelWrapper.getWrappedClass(), localizedTemplatePath, true);
		if (!actualTemplate.getApplicableEntityClass().isAssignableFrom(templateTargetClass)) {
			throw new IllegalTemplateReferenceException(
				"Template named " + actualTemplate.getName() + 
				" expects class " + actualTemplate.getApplicableEntityClass() +
				" but got instance of " + templateTargetClass);
		}
		
				
		templateNames.push(templateReference.getTemplateName());
		templateBasePaths.push(templateReference.getBasePath());
	}
	
	public void popTemplate() {
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
	public String localizePath(String subPath) {
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

	public Errors getErrors() {
		return errors;
	}

	public void setErrors(Errors errors) {
		this.errors = errors;
	}

	public BeanWrapper getModelWrapper() {
		return modelWrapper;
	}

	public void setModelWrapper(BeanWrapper modelWrapper) {
		this.modelWrapper = modelWrapper;
	}

	public SPELResolver getSpelResolver() {
		return spelResolver;
	}

	public void setSpelResolver(SPELResolver spelResolver) {
		this.spelResolver = spelResolver;
	}

	public Stack<String> getNestedPath() {
		return nestedPath;
	}

	public void setNestedPath(Stack<String> nestedPath) {
		this.nestedPath = nestedPath;
	}

	

	public Stack<List<Integer>> getCheckedModelHashes() {
		return checkedModelHashes;
	}

	public void setCheckedModelHashes(Stack<List<Integer>> checkedModelHashes) {
		this.checkedModelHashes = checkedModelHashes;
	}
	
	public Stack<String> getTemplateNames() {
		return templateNames;
	}

	public void setTemplateNames(Stack<String> templateNames) {
		this.templateNames = templateNames;
	}

	public Stack<String> getTemplateBasePaths() {
		return templateBasePaths;
	}

	public void setTemplateBasePaths(Stack<String> templateBasePaths) {
		this.templateBasePaths = templateBasePaths;
	}
	
	public Map<String, String> getCollectionPathReplacements() {
		return collectionPathReplacements;
	}

	public void setCollectionPathReplacements(
			Map<String, String> collectionPathReplacements) {
		this.collectionPathReplacements = collectionPathReplacements;
	}

}
