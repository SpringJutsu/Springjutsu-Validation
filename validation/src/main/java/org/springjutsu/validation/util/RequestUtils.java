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

package org.springjutsu.validation.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

/**
 * A utility class for manipulating requests, or
 * for searching request metadata, etc.
 * @author Clark Duplichien
 *
 */
public class RequestUtils {
	
	/**
	 * A pattern describing a RESTful url variable
	 */
	public static final String PATH_VAR_PATTERN = "\\{[^\\}]+\\}";
	
	/**
	 * Used by successView and validationFailureView.
	 * If the user specifies a path containing RESTful url
	 * wildcards, evaluate those wildcard expressions against 
	 * the current model map, and plug them into the url.
	 * If the wildcard is a multisegmented path, get the top level
	 * bean from the model map, and fetch the sub path using 
	 * a beanwrapper instance.
	 * @param viewName The view potentially containing wildcards
	 * @param model the model map 
	 * @param request the request
	 * @return a wildcard-substituted view name
	 */
	@SuppressWarnings("unchecked")
	public static String replaceRestPathVariables(String viewName, Map<String, Object> model, HttpServletRequest request) {
		String newViewName = viewName;
		Matcher matcher = Pattern.compile(PATH_VAR_PATTERN).matcher(newViewName);
		while (matcher.find()) {
			String match = matcher.group();
			String varName = match.substring(1, match.length() - 1);
			String baseVarName = null;
			String subPath = null;
			if (varName.contains(".")) {
				baseVarName = varName.substring(0, varName.indexOf("."));
				subPath = varName.substring(varName.indexOf(".") + 1);
			} else {
				baseVarName = varName;
			}
			Map<String, String> uriTemplateVariables = 
				(Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
			if (uriTemplateVariables != null && uriTemplateVariables.containsKey(varName)) {
				newViewName = newViewName.replace(match, String.valueOf(uriTemplateVariables.get(varName)));
			} else {
				Object resolvedObject = model.get(baseVarName);
				if (resolvedObject == null) {
					throw new IllegalArgumentException(varName + " is not present in model.");
				}
				if (subPath != null) {
					BeanWrapperImpl beanWrapper = new BeanWrapperImpl(resolvedObject);
					resolvedObject = beanWrapper.getPropertyValue(subPath);
				}
				if (resolvedObject == null) {
					throw new IllegalArgumentException(varName + " is not present in model.");
				}
				newViewName = newViewName.replace(match, String.valueOf(resolvedObject));
			}
			matcher.reset(newViewName);
		}
		return newViewName;
	}

	/**
	 * Identifies the view name pattern which best matches
	 * the current request URL (path within handler mapping).
	 * @param candidateViewNames The view name patterns to test
	 * @param controllerPaths Possible request mapping prefixes 
	 * from a controller-level RequestMapping annotation 
	 * @param request the current request
	 * @return the best matching view name.
	 */
	public static String findFirstMatchingRestPath(String[] candidateViewNames, 
			String[] controllerPaths, HttpServletRequest request) {
		
		String pathWithinHandlerMapping = removeLeadingAndTrailingSlashes(
			(String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE));
		
		AntPathMatcher antPathMatcher = new AntPathMatcher();
		for (String candidatePath : candidateViewNames) {
			if ((controllerPaths == null || candidatePath.startsWith("/")) 
					&& antPathMatcher.match(removeLeadingAndTrailingSlashes(candidatePath), pathWithinHandlerMapping)) {
				return candidatePath;
			} else if (controllerPaths != null) {
				for (String controllerPath : controllerPaths) {
					String testPath = (controllerPath + "/" + candidatePath).replace("//", "/");
					if (antPathMatcher.match(removeLeadingAndTrailingSlashes(testPath), pathWithinHandlerMapping)) {
						return candidatePath;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Given a handler object, return the base controller
	 * class-level requestMapping paths. In case the controller
	 * specifies one or more base path(s).
	 * @param handler the handler object
	 * @return the controller request paths.
	 */
	public static String[] getControllerRequestPaths(HandlerMethod handler) {
		RequestMapping requestMapping = AnnotationUtils.findAnnotation(
				AopUtils.getTargetClass(handler.getMethod().getDeclaringClass()), RequestMapping.class);
		return requestMapping == null ? null : requestMapping.value();
	}
	
	/**
	 * Removes the leading and trailing slashes from a url path.
	 * @param path the path
	 * @return the path without leading and trailing slashes.
	 */
	public static String removeLeadingAndTrailingSlashes(String path) {
		String newPath = path;
		if (newPath.startsWith("/")) {
			newPath = newPath.substring(1);
		}
		if (newPath.endsWith("/")) {
			newPath = newPath.substring(0, newPath.length() - 1);
		}
		return newPath;
	}
	
}
