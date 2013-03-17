package org.springjutsu.validation.exceptions;

public class CircularValidationTemplateReferenceException extends IllegalStateException {

	private static final long serialVersionUID = 1L;

	public CircularValidationTemplateReferenceException(String message) {
		super(message);
	}

}
