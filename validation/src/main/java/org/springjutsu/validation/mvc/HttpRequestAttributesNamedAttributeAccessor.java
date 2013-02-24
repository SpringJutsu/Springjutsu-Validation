package org.springjutsu.validation.mvc;

import javax.servlet.http.HttpServletRequest;

import org.springjutsu.validation.spel.AbstractNamedAttributeAccessor;

public class HttpRequestAttributesNamedAttributeAccessor extends AbstractNamedAttributeAccessor {
	
	private HttpServletRequest request;
	
	public HttpRequestAttributesNamedAttributeAccessor(HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public Object get(String key) {
		return request.getAttribute(key);
	}

	@Override
	public void set(String key, Object value) {
		request.setAttribute(key, value);
	}

}
