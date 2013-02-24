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

package org.springjutsu.validation.util;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
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
			if (!uriTemplateVariables.containsKey(varName))
			{
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
				newViewName = newViewName.replace(match, resolvedObject.toString());
			}
			else
			{
				newViewName = newViewName.replace(match, uriTemplateVariables.get(varName));
			}
			matcher.reset(newViewName);
		}
		return newViewName;
	}

	/**
	 * Since successView and validationFailureView can contain
	 * multiple possible redirection paths using the format
	 * incomingRequestPath=outgoingRedirectPath, we'll need to 
	 * identify which incomingRequestPath best matches the 
	 * request that generated this validation, and then return
	 * the view name (outgoingRedirectPath) associated with that
	 * path.
	 * This is made simple by looking for the bestMatchingPattern
	 * that spring has already kindly exposed in the request 
	 * attributes.
	 * @param candidateViewNames The possible views
	 * @param controllerPaths The base path(s) from the controller
	 * @param request the request
	 * @return the best matching view name to redirect to.
	 */
	public static String findMatchingRestPath(String[] candidateViewNames, 
			String[] controllerPaths, HttpServletRequest request) {
		String bestMatch = null;
		String requestBestMatchingPattern = 
			(String) request.getAttribute("org.springframework.web.servlet.HandlerMapping.bestMatchingPattern");
		String preferredPath = requestBestMatchingPattern != null ? 
				requestBestMatchingPattern
				.replaceAll(PATH_VAR_PATTERN, "VAR") : null;
		for (String candidate : candidateViewNames) {
			if (!candidate.contains("=") && bestMatch == null) {
				bestMatch = candidate;
			} else if (preferredPath != null && candidate.contains("=")) {
				String candidatePath = candidate.substring(0, candidate.indexOf("="))
				.replaceAll(PATH_VAR_PATTERN, "VAR")
				.replaceAll("\\*\\*/?", "(*/?)+")
				.replace("*", "[^/]+");
				if ((controllerPaths == null || candidatePath.startsWith("/"))&& preferredPath.matches(candidatePath)) {
					return candidate.substring(candidate.indexOf("=") + 1);
				} else if (controllerPaths != null) {
					for (String controllerPath : controllerPaths) {
						String controllerPathRegex = 
							controllerPath
							.replaceAll(PATH_VAR_PATTERN, "VAR")
							.replaceAll("\\*\\*/?", "(*/?)+")
							.replace("*", "[^/]+");
						String testPath = ( controllerPathRegex + "/" + candidatePath).replace("//", "/");
						if (preferredPath.matches(testPath)) {
							return candidate.substring(candidate.indexOf("=") + 1);
						}
					}
				}
			}
		}
		return bestMatch;
	}
	
	/**
	 * Given a handler object, return the base controller
	 * class-level requestMapping paths. In case the controller
	 * specifies one or more base path(s).
	 * @param handler the handler object
	 * @return the controller request paths.
	 */
	public static String[] getControllerRequestPaths(HandlerMethod handler) {
		RequestMapping requestMapping = (RequestMapping) findHandlerAnnotation(handler.getMethod().getDeclaringClass(), RequestMapping.class);
		return requestMapping == null ? null : requestMapping.value();
	}
	
	/**
	 * Find an annotation on the possibly proxied handler.
	 * @param handler The handler / controller
	 * @param annotationClass The annotation to find.
	 * @return The annotation, or null if not present.
	 */
	@SuppressWarnings("unchecked")
	public static Annotation findHandlerAnnotation(Class handlerClass, Class annotationClass) {
		Class controllerClass = handlerClass;
		while (controllerClass.getAnnotation(annotationClass) == null 
				&& controllerClass.getSuperclass() != null) {
			controllerClass = controllerClass.getSuperclass();
		}
		return controllerClass.getAnnotation(annotationClass);		
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
