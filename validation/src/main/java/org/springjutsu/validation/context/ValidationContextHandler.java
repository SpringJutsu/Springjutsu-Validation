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
	 * @return true if any of the qualifiers represent an
	 * event or occurrence which is ongoing and should have
	 * the declared rules evaluated.
	 */
	boolean isActive(Set<String> qualifiers);
	
	/**
	 * Allows the ValidationContextHandler to initialize the
	 * SPELResolver with scope and parser customizations 
	 * useful in evaluating the current context.
	 * @param spelResolver The SPELResolver instance to initialize
	 */
	void initializeSPELResolver(SPELResolver spelResolver);

}
