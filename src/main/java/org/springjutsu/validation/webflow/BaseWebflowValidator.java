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

package org.springjutsu.validation.webflow;

import java.lang.reflect.ParameterizedType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springjutsu.validation.ValidationManager;

/**
 * An extensible / parameterizable base validator for use
 * in Spring Webflow. Currently have to use this since there's
 * no option in webflow to inject a custom @link{ValidationHelper}
 * instance.
 * So, users can extend and parameterize this instead, and register
 * it as a validator bean, and bam nifty, it'll call validation.
 * Chose this one because it supports the Errors object.
 * The user can also perform validation outside of what's in XML
 * by overriding the #doManualValidation method.
 * @see Validator 
 * @see #doManualValidation(String, String, Object, Errors)
 * 
 * @author Clark Duplichien
 *
 * @param <T> Class which this validator validates
 */
public class BaseWebflowValidator<T> implements Validator {
	
	/**
	 * Will delegate to the validation manager to 
	 * perform validation defined in XML rule sets.
	 */
	@Autowired
	private ValidationManager validationManager;
	
	/**
	 * The parameterization-specified class of which an
	 * instance will be validated by this validator. 
	 */
	protected Class<T> entityClass;
	
	/**
	 * Default constructor. Sets entity class from
	 * the parameterized argument.
	 */
	public BaseWebflowValidator() {
		this.entityClass = calculateEntityClass();
	}
	
	/**
	 * Calculates the class specified in the parameterization.
	 * @return the parameterized class.
	 */
	@SuppressWarnings("unchecked")
	protected Class<T> calculateEntityClass() {
		Object clazz = ((ParameterizedType) getClass()
				.getGenericSuperclass()).getActualTypeArguments()[0];
		if (ParameterizedType.class.isAssignableFrom(clazz.getClass())) {
			return (Class<T>) ((ParameterizedType) clazz).getRawType();
		} else {
			return (Class<T>) clazz;
		}
	}

	/**
	 * Supports a class if this class was parameterized
	 * for it, and if the validation manager has rules for it.
	 * @param clazz The class to check support for
	 * @return true if supported.
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return entityClass.isAssignableFrom(clazz) 
			&& validationManager.supports(clazz);
	}

	/**
	 * Perform actual validation.
	 * Delegate to the validation manager, and then
	 * provide the flow and state id to the doManualValidation
	 * method in case the user has some specific things needing
	 * validation.
	 * @param target the base object to validate
	 * @param errors the Errors object on which to record errors.
	 */
	@Override
	public void validate(Object target, Errors errors) {
		validationManager.validate(target, errors);
		if (RequestContextHolder.getRequestContext() != null) {
			String stateId = 
				RequestContextHolder.getRequestContext().getCurrentState().getId();
			String flowId = 
				RequestContextHolder.getRequestContext().getCurrentState().getOwner().getId();
			doManualValidation(flowId, stateId, target, errors);
		}
	}
	
	/**
	 * Overload doManualValidation if it is wished to perform 
	 * validation in addition to what is specified in the rules.
	 * @param flowId Provides the Web Flow Flow ID.
	 * @param stateId Provides the Web Flow State ID.
	 * @param target Provides the Object being validated.
	 * @param errors Provides an Errors interface for recording validation messages.
	 */
	public void doManualValidation(String flowId, String stateId, Object target, Errors errors) {
		return;
	}

}
