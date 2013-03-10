package org.springjutsu.validation.context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springjutsu.validation.namespace.KeyedBeanRegistrant;

public class ValidationContextHandlerContainer {
	
	/**
	 * A map of context name to handler.
	 */
	protected Map<String, ValidationContextHandler> validationContextHandlers = 
		new HashMap<String, ValidationContextHandler>();
	
	/**
	 * A list of context handlers registered as beans
	 */
	protected List<KeyedBeanRegistrant> beanRegistrants;
	
	/**
	 * Can configure this to false if user doesn't want the default handlers. 
	 */
	protected boolean addDefaultContextHandlers = true;
	
	/**
	 * Use the bean factory to look up annotated rule executors.
	 */
	@Autowired
	protected BeanFactory beanFactory;
	
	/**
	 * Finds the annotated context handlers by searching the bean factory.
	 * Also registers XML-configured context handlers.
	 * @throws BeansException on a bad.
	 */
	@PostConstruct
	public void registerContextHandlers() throws BeansException {
		if (addDefaultContextHandlers) {
			addDefaultContextHandlers();
		}
		Map<String, Object> contextHandlerBeans = 
			((ListableBeanFactory) beanFactory).getBeansWithAnnotation(ConfiguredContextHandler.class);

		for (String springName : contextHandlerBeans.keySet()) {
			ValidationContextHandler handler = (ValidationContextHandler) contextHandlerBeans.get(springName);
			String contextType = handler.getClass().getAnnotation(ConfiguredContextHandler.class).type();
			setCustomContextHandler(contextType, handler);
		}
		if (beanRegistrants != null) {
			for (KeyedBeanRegistrant registrant : beanRegistrants) {
				setCustomContextHandler(registrant.getKey(), 
						(ValidationContextHandler) beanFactory.getBean(registrant.getBeanName()));
			}
		}
	}

	/**
	 * Set custom context handlers for specific types
	 * @param customContextHandlers the context handlers to set
	 */
	public void setCustomRuleExecutors(Map<String, ValidationContextHandler> customContextHandlers) {
		for (String contextType : customContextHandlers.keySet()) {
			setCustomContextHandler(contextType, customContextHandlers.get(contextType));
		}
	}
	
	/**
	 * Set custom context handler for a specific type.
	 * Throws IllegalArgumentException if a context 
	 * handler is already registered for the given type.
	 * @param contextType the context type the handler handles
	 * @param contextHandler The context handler to set
	 */
	public void setCustomContextHandler(String contextType, ValidationContextHandler contextHandler) {
		if (validationContextHandlers.containsKey(contextType)) {
			throw new IllegalArgumentException(
				"Handler for context type \"" + contextType 
				+ "\" already set to type " 
				+ validationContextHandlers.get(contextType).getClass().getCanonicalName());
		} else {
			validationContextHandlers.put(contextType, contextHandler);
		}
	}
	
	/**
	 * Gets the context handler for the given context type
	 * @param contextType the type to acquire the handler for
	 * @return ValidationContextHandler for the given type
	 */
	public ValidationContextHandler getContextHandlerForType(String contextType) {
		if (validationContextHandlers.containsKey(contextType)) {
			return validationContextHandlers.get(contextType);
		}
		throw new IllegalArgumentException("No context handler registered for context type: " + contextType);
	}
	
	/**
	 * Instantiates and adds handlers for default types
	 */
	protected void addDefaultContextHandlers() {
		setCustomContextHandler("form", new MVCFormValidationContextHandler());
		setCustomContextHandler("webflow", new WebflowValidationContextHandler());
		setCustomContextHandler("group", new ValidationGroupContextHandler());
	}
	
	/**
	 * Set to false if user does not want the default rule executors.
	 * @param addDefaultRuleExecutors
	 */
	public void setAddDefaultContextHandlers(boolean addDefaultContextHandlers) {
		this.addDefaultContextHandlers = addDefaultContextHandlers;
	}
	
	/**
	 * Hook by which @see{ValidationConfigurationParser} registers XML defined rule executors
	 * @param registrants @see{ValidationConfigurationParser} RuleExecutorBeanRegistrants to register.
	 */
	public void setContextHandlerBeanRegistrants(List<KeyedBeanRegistrant> registrants) {
		this.beanRegistrants = registrants;
	}

}
