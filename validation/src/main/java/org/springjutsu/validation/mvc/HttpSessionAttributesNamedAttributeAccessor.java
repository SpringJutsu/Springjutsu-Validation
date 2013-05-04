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

import org.springjutsu.validation.spel.AbstractNamedAttributeAccessor;

/**
 * Provides Map-like SPEL access semantics for Session Attributes.
 * @author Clark Duplichien
 *
 */
public class HttpSessionAttributesNamedAttributeAccessor extends AbstractNamedAttributeAccessor {

	private HttpServletRequest request;
	
	/**
	 * Default Constructor
	 * @param request the current request
	 */
	public HttpSessionAttributesNamedAttributeAccessor(HttpServletRequest request) {
		this.request = request;
	}
	
	/**
	 * Allow writing if the session has been initialized
	 */
	@Override
	public boolean isWritable() {
		return request.getSession(false) != null;
	}
	
	/**
	 * Return the value of the session attribute if the session has been initialized.
	 */
	@Override
	public Object get(String key) {
		return request.getSession(false) == null ? null : request.getSession(false).getAttribute(key);
	}

	/**
	 * Set the value of the session attribute if the session has been initialized.
	 */
	@Override
	public void set(String key, Object value) {
		if (request.getSession(false) != null) {
			request.getSession(false).setAttribute(key, value);
		}
	}

	
	
}
