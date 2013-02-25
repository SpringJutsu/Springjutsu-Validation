package org.springjutsu.validation.context;

import java.util.Set;

import org.springjutsu.validation.spel.SPELResolver;

/**
 * Responsible for determining whether or
 * not a specific ValidationContext is active,
 * by evaluating whether or not the event
 * or occurrence specified by the type and 
 * qualifier is currently undergoing.
 * The type of ValidationContext handled by 
 * this ValidationContextHandler is specified
 * when registering the ValidationContextHandler
 * with the validation configuration.
 * @author Clark Duplichien
 *
 */
public interface ValidationContextHandler {
	
	/**
	 * Determine if the context type is active 
	 * for any of the given qualifiers.
	 * @param qualifiers A set of qualifiers indicating a 
	 * specific event or occurrence of the type handled
	 * by this ValidationContextHandler
	 * @param rootModel The actual root object passed in
	 * to be validated
	 * @param validationHints The JSR-303 group identifiers,
	 * or spring validation hints, which indicate the validation
	 * groups specified to the validation manager.
	 * @return true if any of the qualifiers represent an
	 * event or occurrence which is ongoing and should have
	 * the declared rules evaluated.
	 */
	boolean isActive(Set<String> qualifiers, Object rootModel, String[] validationHints);
	
	/**
	 * Allows the context handler to specify whether or not
	 * rules under the context should be run during recursive 
	 * sub bean validation. 
	 * If your context is based entirely off of the root object 
	 * being validated, for example validating the backing 
	 * object of a form or web service endpoint, then it is 
	 * advisable to disable the context when sub-beans are 
	 * being evaluated, else the rules which were intended 
	 * to be run for only the root object may be evaluated 
	 * against a different object of the same class within
	 * the sub bean paths. 
	 * However, if the context is not intended to be evaluated
	 * only against the root object, and instead implies some 
	 * contextual condition for general application of rules 
	 * throughout the object model, it would be preferable to
	 * enable the context during recursive sub bean validation.
	 * @return true if the validation rules contained in the
	 * context declaration should be run for sub beans under
	 * the root object passed to the validator.
	 */
	boolean enableDuringSubBeanValidation();
	
	/**
	 * Allows the ValidationContextHandler to initialize the
	 * SPELResolver with scope and parser customizations 
	 * useful in evaluating the current context.
	 * @param spelResolver The SPELResolver instance to initialize
	 */
	void initializeSPELResolver(SPELResolver spelResolver);

}
