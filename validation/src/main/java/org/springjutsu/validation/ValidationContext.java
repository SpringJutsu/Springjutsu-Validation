package org.springjutsu.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.validation.Errors;
import org.springjutsu.validation.spel.WebContextSPELResolver;
import org.springjutsu.validation.util.PathUtils;

public class ValidationContext {
	
	private Errors errors;
	private BeanWrapper modelWrapper;
	private WebContextSPELResolver spelResolver;
	private String currentForm;
	private Stack<String> nestedPath;
	private String collectionPath;
	private String currentCollectionMemberPath;
	
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
	
	public ValidationContext(Object model, Errors errors, String currentForm) {
		this.errors = errors;
		this.modelWrapper = model == null ? null : new BeanWrapperImpl(model);
		this.spelResolver = new WebContextSPELResolver(model);
		this.currentForm = currentForm;
		this.nestedPath = new Stack<String>();
		this.checkedModelHashes = new Stack<List<Integer>>();
		checkedModelHashes.push(new ArrayList<Integer>());
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
	
	public Object pushNestedPath(String subPath) {
		nestedPath.push(subPath);
		errors.pushNestedPath(subPath);
		checkedModelHashes.push(new ArrayList<Integer>(checkedModelHashes.peek()));
		return getBeanAtNestedPath();
	}
	
	public void popNestedPath() {
		nestedPath.pop();
		errors.popNestedPath();
		checkedModelHashes.pop();
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

	public WebContextSPELResolver getSpelResolver() {
		return spelResolver;
	}

	public void setSpelResolver(WebContextSPELResolver spelResolver) {
		this.spelResolver = spelResolver;
	}

	public String getCurrentForm() {
		return currentForm;
	}

	public void setCurrentForm(String currentForm) {
		this.currentForm = currentForm;
	}

	public Stack<String> getNestedPath() {
		return nestedPath;
	}

	public void setNestedPath(Stack<String> nestedPath) {
		this.nestedPath = nestedPath;
	}

	public String getCollectionPath() {
		return collectionPath;
	}

	public void setCollectionPath(String collectionPath) {
		this.collectionPath = collectionPath;
	}

	public String getCurrentCollectionMemberPath() {
		return currentCollectionMemberPath;
	}

	public void setCurrentCollectionMemberPath(String currentCollectionMemberPath) {
		this.currentCollectionMemberPath = currentCollectionMemberPath;
	}

	public Stack<List<Integer>> getCheckedModelHashes() {
		return checkedModelHashes;
	}

	public void setCheckedModelHashes(Stack<List<Integer>> checkedModelHashes) {
		this.checkedModelHashes = checkedModelHashes;
	}

}
