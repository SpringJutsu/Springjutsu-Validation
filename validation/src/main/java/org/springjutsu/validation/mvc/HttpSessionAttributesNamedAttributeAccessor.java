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
