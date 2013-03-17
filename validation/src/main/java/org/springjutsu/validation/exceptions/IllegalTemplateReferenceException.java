package org.springjutsu.validation.exceptions;

public class IllegalTemplateReferenceException extends IllegalArgumentException {

	private static final long serialVersionUID = 1L;

	public IllegalTemplateReferenceException(String message) {
		super(message);
	}
}
