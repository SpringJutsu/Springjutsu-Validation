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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;
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

	private AntPathMatcher antPathMatcher = new AntPathMatcher();
	
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
			return appliesToForm(RequestUtils.getPathWithinHandlerMapping(), qualifiers);
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
	 * request parameters, request attributes,
	 * MVC Path Variables and session attributes.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void initializeSPELResolver(SPELResolver spelResolver) {
		
		// initialize property accessors
		spelResolver.getScopedContext().addPropertyAccessor(new NamedAttributeAccessorPropertyAccessor());
		
		// access scoped request
		RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
		
		// get path variable map
		Map<String, String> uriTemplateVars = 
			(Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
		if (uriTemplateVars == null) {
			uriTemplateVars = new HashMap<String, String>();
		}
		
		// initialize named contexts
		spelResolver.getScopedContext().addContext("requestParameters", 
				new HttpRequestParametersNamedAttributeAccessor(request));
		spelResolver.getScopedContext().addContext("pathVariables", uriTemplateVars);
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
	
	/** Returns true if the rule applies to the current form.
	 * Replace any REST variable wildcards with wildcard regex.
	 * Replace ant path wildcards with wildcard regexes as well.
	 * Iterate through possible form names to find the first match.
	 */
	public boolean appliesToForm(String rawForm, Set<String> candidateForms) {
		String form = rawForm == null ? "" : rawForm;
		boolean appliesToForm = false;
		
		for (String formName : candidateForms) {
			if (antPathMatcher.match(RequestUtils.removeLeadingAndTrailingSlashes(formName), form)) {
				appliesToForm = true;
				break;
			}
		}
		
		return appliesToForm;
	}

}
