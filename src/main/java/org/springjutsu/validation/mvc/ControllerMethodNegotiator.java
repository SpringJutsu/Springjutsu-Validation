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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;

/**
 * A reflection-based dirty hack class used to get the  
 * controller method that executed for the current response. 
 * Since there's no hook to grab that method when coming from 
 * an exception handler, this forcibly unwraps it.
 * Would prefer to see this class disappear...
 * TODO: SPR-8178
 * @author Clark Duplichien
 *
 */
public class ControllerMethodNegotiator {
	
	/**
	 * The handler adapter that we're going to extract
	 * the called method from.
	 */
	@Autowired
	private AnnotationMethodHandlerAdapter handlerAdapter;
	
	/**
	 * Get the handler method called for the request.
	 * @param handler The handler instance
	 * @param request The request
	 * @return the handler method for the request.
	 */
	public Method getControllerMethod(Object handler, HttpServletRequest request) {
		Map methodResolverCache = getMethodResolverCache();
		if (methodResolverCache == null) {
			return null;
		}

		//TODO: make this handle proxied objects better
		Object methodResolver = methodResolverCache.get(handler.getClass().getSuperclass());
		if (methodResolver == null) {
			methodResolver = methodResolverCache.get(handler.getClass());
			if (methodResolver == null) {
				return null;
			}
		}
		Method method = getMethod(methodResolver, request);
		return method;		
	}
	
	/**
	 * A utility method used to extract a specified annotation 
	 * from the handler method for the request
	 * @param handler The handler instance
	 * @param request The request
	 * @param annotationClass Class of the annotation to extract
	 * @return The Annotation
	 */
	public Annotation getAnnotationFromControllerMethod(Object handler, 
			HttpServletRequest request, Class annotationClass) {
		Method method = getControllerMethod(handler, request);
		if (method == null) {
			return null;
		}
		Annotation annotation = method.getAnnotation(annotationClass);
		return annotation;
	}
	
	/**
	 * Forcibly calls resolveHandlerMethod to resolve the method
	 * that executes for a request.
	 * @param methodResolver the method resolver extracted from cache
	 * @param request The request
	 * @return the handler method
	 */
	private Method getMethod(Object methodResolver, HttpServletRequest request) {
		Method method = null;
		try {
			Method methodMethod = methodResolver.getClass().getDeclaredMethod("resolveHandlerMethod", HttpServletRequest.class);
			ReflectionUtils.makeAccessible(methodMethod);
			method = (Method) methodMethod.invoke(methodResolver, request);
		} catch (Exception e) {
			throw new IllegalStateException("Couldn't identify handler method", e);
		}
		return method;
	}

	/**
	 * Forcibly extracts the method resolver cache from
	 * the handler adapter in order to get the methodResolver
	 * @return Map the method resolver cache
	 */
	private Map getMethodResolverCache() {
		Map cache = null;
		try {
			Field methodCacheField = handlerAdapter.getClass().getDeclaredField("methodResolverCache");
			ReflectionUtils.makeAccessible(methodCacheField);
			cache = (Map) methodCacheField.get(handlerAdapter);
		} catch (Exception e) {
			throw new IllegalStateException("Couldn't identify handler method", e);
		}
		return cache;
	}

}
