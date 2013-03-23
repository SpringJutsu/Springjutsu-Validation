
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

package org.springjutsu.validation.mvc;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springjutsu.validation.mvc.annotations.ValidationFailureView;
import org.springjutsu.validation.mvc.annotations.ValidationFailureViews;
import org.springjutsu.validation.util.RequestUtils;

/**
 * Handles the implementation of the @link{ValidationFailureView} annotation.
 * @author Clark Duplichien
 */
public class ValidationFailureViewHandlerExceptionResolver implements HandlerExceptionResolver {

	/**
	 * When a BindException is caught, purportedly because some validation
	 * using the @link{Valid} annotated validation failed, redirect to the view
	 * indicated by the @link{ValidationFailureView} annotation of the controller
	 * method, and attach any validation errors messages.
	 * In the event that there are multiple possible 
	 * validation failure paths defined for the validation failure view, 
	 * will attempt to isolate the closest matching url-path key and redirect 
	 * to the value path, as described in @link{ValidationFailureView}
	 */
	public ModelAndView resolveException(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex) {
		
		if (!(ex instanceof BindException)) {
			return null;
		}
		
		if (!(handler instanceof HandlerMethod)) {
			return null;
		}
		
		ModelAndView mav = null;
		ValidationFailureViews failViews = (ValidationFailureViews) ((HandlerMethod) handler).getMethodAnnotation(ValidationFailureViews.class);
		ValidationFailureView failView = (ValidationFailureView) ((HandlerMethod) handler).getMethodAnnotation(ValidationFailureView.class);
		String viewName = null;
		
		if (failView != null) {
			viewName = failView.targetUrl();
		} else if (failViews != null) {
			String[] controllerPaths = RequestUtils.getControllerRequestPaths((HandlerMethod) handler);
			viewName = findMatchingTargetUrl(failViews.value(), controllerPaths, request);
		}
		
		if (viewName == null) {
			return null;
		}
		
		viewName = RequestUtils.replaceRestPathVariables(viewName, ((BindException)ex).getModel(), request);
		mav = new ModelAndView(viewName, ((BindException)ex).getModel());
		
		return mav;
	}

	protected String findMatchingTargetUrl(ValidationFailureView[] failViews, String[] controllerPaths, HttpServletRequest request) {
		Map<String, String> sourceTargetMap = new HashMap<String, String>();
		for (ValidationFailureView failView : failViews) {
			if (failView.sourceUrl().isEmpty()) {
				throw new IllegalArgumentException("sourceUrl is required when specifying multiple success or failure views.");
			}
			
			if (sourceTargetMap.containsKey(failView.sourceUrl())) {
				throw new IllegalArgumentException("duplicate sourceUrl when specifying multiple success or failure views: " + failView.sourceUrl());
			}
			
			sourceTargetMap.put(failView.sourceUrl(), failView.targetUrl());
		}
		
		String matchingSourceUrl = RequestUtils.findFirstMatchingRestPath(
				sourceTargetMap.keySet().toArray(new String[sourceTargetMap.size()]), controllerPaths, request);
		
		return matchingSourceUrl == null ? null : sourceTargetMap.get(matchingSourceUrl);		
	}
	
}
