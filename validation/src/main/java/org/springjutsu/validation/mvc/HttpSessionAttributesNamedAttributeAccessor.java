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

public class HttpSessionAttributesNamedAttributeAccessor extends AbstractNamedAttributeAccessor {

	private HttpServletRequest request;
	
	public HttpSessionAttributesNamedAttributeAccessor(HttpServletRequest request) {
		this.request = request;
	}
	
	@Override
	public boolean isWritable() {
		return request.getSession(false) != null;
	}
	
	@Override
	public Object get(String key) {
		return request.getSession(false) == null ? null : request.getSession(false).getAttribute(key);
	}

	@Override
	public void set(String key, Object value) {
		if (request.getSession(false) != null) {
			request.getSession(false).setAttribute(key, value);
		}
	}

	
	
}
