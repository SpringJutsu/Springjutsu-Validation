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

package org.springjutsu.validation.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.RequestToViewNameTranslator;

/**
 * Responsible for redirecting to the previous view
 * when validation errors occur under an MVC controller model.
 * Attempts to utilize a wired view translator. If none exists, 
 * or if view translator is unable to determine the correct view,
 * fall back by utilizing the request's servlet path. 
 * @author Clark Duplichien
 *
 */
public class ViewTranslatorValidationFailureRedirector implements HandlerExceptionResolver {
	
	Log log = LogFactory.getLog(ViewTranslatorValidationFailureRedirector.class);
	
	@Autowired(required=false)
	RequestToViewNameTranslator viewTranslator;

	public ModelAndView resolveException(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex) {
		ModelAndView mav = null;
		if (ex instanceof BindException) {
			String view = null;
			if (viewTranslator != null) {
				try {
					view = viewTranslator.getViewName(request);
				} catch (Exception e) {
					log.error("view translator could not determine view name, utilizing servlet path instead", e);
				}
			}
			if (view == null) {
				view = request.getServletPath();
			}
			mav = new ModelAndView(view, ((BindException)ex).getModel());
		}
		
		return mav;
	}

}
