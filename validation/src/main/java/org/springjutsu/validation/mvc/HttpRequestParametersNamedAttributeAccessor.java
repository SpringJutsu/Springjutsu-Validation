package org.springjutsu.validation.mvc;

import javax.servlet.http.HttpServletRequest;

import org.springjutsu.validation.spel.AbstractNamedAttributeAccessor;

public class HttpRequestParametersNamedAttributeAccessor extends AbstractNamedAttributeAccessor {
	
	private HttpServletRequest request;
	
	public HttpRequestParametersNamedAttributeAccessor(HttpServletRequest request) {
		this.request = request;
	}
	
	@Override
	public boolean isWritable() {
		return false;
	}

	@Override
	public Object get(String key) {
		String[] paramValues = (request.getParameterValues(key));
		if (paramValues != null && paramValues.length == 1) {
			return paramValues[0];
		} else {
			return paramValues;
		}
	}

	@Override
	public void set(String key, Object value) {
		throw new UnsupportedOperationException();
	}

}
