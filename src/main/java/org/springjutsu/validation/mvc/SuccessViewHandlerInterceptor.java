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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springjutsu.validation.mvc.annotations.SuccessView;
import org.springjutsu.validation.util.RequestUtils;

/**
 * Handles the implementation of the @link{SuccessView} annotation.
 * Self-registering bean that places itself at the end of the interceptor
 * chain on a handler adapter.
 * @author Clark Duplichien
 *
 */
public class SuccessViewHandlerInterceptor extends HandlerInterceptorAdapter {
	
	/**
	 * Used to discover the most recently called
	 * controller method in order to find the 
	 * relevant successView annotation.
	 */
	@Autowired
	private ControllerMethodNegotiator controllerMethodNegotiator;
	
	/**
	 * Responsible for redirecting to the view defined
	 * in the @link{SuccessView} annotation on the current
	 * handler method.
	 * In the event that there are multiple possible 
	 * success paths defined for the success view, 
	 * will attempt to isolate the closest matching
	 * url-path key and redirect to the value path,
	 * as described in @link{SuccessView}
	 */
	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		
		SuccessView successView = (SuccessView)
			controllerMethodNegotiator.getAnnotationFromControllerMethod(
				handler, request, SuccessView.class);
		
		if (successView == null) {
			return;
		}
				
		String[] candidateViewNames = successView.value();
		String[] controllerPaths = RequestUtils.getControllerRequestPaths(handler);
		String viewName = RequestUtils.findMatchingRestPath(candidateViewNames, controllerPaths, request);
		
		if (viewName == null) {
			return;
		}
		
		viewName = RequestUtils.replaceRestPathVariables(viewName, modelAndView.getModel(),  request);
		modelAndView.setViewName(viewName);
	}
}
