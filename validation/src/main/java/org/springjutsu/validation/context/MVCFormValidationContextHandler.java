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

package org.springjutsu.validation.context;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springjutsu.validation.mvc.HttpRequestAttributesNamedAttributeAccessor;
import org.springjutsu.validation.mvc.HttpRequestParametersNamedAttributeAccessor;
import org.springjutsu.validation.mvc.HttpSessionAttributesNamedAttributeAccessor;
import org.springjutsu.validation.spel.NamedAttributeAccessorPropertyAccessor;
import org.springjutsu.validation.spel.SPELResolver;
import org.springjutsu.validation.util.RequestUtils;

/**
 * Handles validation contexts of type "form".
 * Determines whether or not the given form path 
 * matches the current form being submitted
 * via spring MVC.
 * @author Clark Duplichien
 */
public class MVCFormValidationContextHandler implements ValidationContextHandler {

	/**
	 * Will return true if the current request is
	 * a spring MVC request, and the given qualifier 
	 * stating an MVC form path matches the current
	 * MVC request path.
	 */
	@Override
	public boolean isActive(Set<String> qualifiers, Object rootModel, String[] validationHints) {
		if (!isMVCRequest()) {
			return false;
		} else {
			return appliesToForm(getMVCFormName(), qualifiers);
		}
	}
	
	/**
	 * Since form rules are rooted at form-backing object,
	 * we should disable evaluation of form rules during
	 * sub bean validation in order to prevent an object
	 * of the same class as the form-backing object within
	 * the form-backing object's sub beans from being
	 * inadvertently used as the base for another application
	 * of the form-specific rules.
	 */
	@Override
	public boolean enableDuringSubBeanValidation() {
		return false;
	}
	
	/**
	 * Initialize SPEL access to MVC scopes including
	 * request parameters, request attributes and
	 * session attributes.
	 */
	@Override
	public void initializeSPELResolver(SPELResolver spelResolver) {
		
		// initialize property accessors
		spelResolver.getScopedContext().addPropertyAccessor(new NamedAttributeAccessorPropertyAccessor());
		
		// access scoped request
		RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
		
		// initialize named contexts
		spelResolver.getScopedContext().addContext("requestParameters", 
				new HttpRequestParametersNamedAttributeAccessor(request));
		spelResolver.getScopedContext().addContext("requestAttributes", 
				new HttpRequestAttributesNamedAttributeAccessor(request));
		spelResolver.getScopedContext().addContext("session", 
				new HttpSessionAttributesNamedAttributeAccessor(request));
	}
	
	/**
	 * @return true if the current web request is associated
	 * with Spring MVC.
	 */
	public static boolean isMVCRequest() {
		return RequestContextHolder.getRequestAttributes() != null;
	}
	
	/**
	 * Just cleans up a Servlet path URL for rule resolving by
	 * the rules container.
	 * Restful URL paths may be used, with \{variable} path support.
	 * As of 0.6.1, ant paths like * and ** may also be used.
	 */
	protected String getMVCFormName() {
		RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
		return RequestUtils.removeLeadingAndTrailingSlashes(request.getServletPath());
	}
	
	/** Returns true if the rule applies to the current form.
	 * Replace any REST variable wildcards with wildcard regex.
	 * Replace ant path wildcards with wildcard regexes as well.
	 * Iterate through possible form names to find the first match.
	 */
	public boolean appliesToForm(String form, Set<String> candidateForms) {
		if (form == null || form.isEmpty()) {
			return true;
		}
		boolean appliesToForm = candidateForms.isEmpty();
		for (String formName : candidateForms) {
			String formPattern = 
				formName.replaceAll("\\{[^\\}]*}", "[^/]+")
				.replaceAll("\\*\\*/?", "(*/?)+")
				.replace("*", "[^/]+");
			if (form.matches(formPattern)) {
				appliesToForm = true;
				break;
			}			
		}
		return appliesToForm;
	}

}
