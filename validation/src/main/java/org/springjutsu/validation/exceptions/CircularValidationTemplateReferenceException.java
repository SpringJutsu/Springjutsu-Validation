package org.springjutsu.validation.exceptions;

public class CircularValidationTemplateReferenceException extends IllegalStateException {
	
	public CircularValidationTemplateReferenceException(String message) {
		super(message);
	}

}
