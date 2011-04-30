package org.springjutsu.validation.namespace;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springjutsu.validation.mvc.ControllerMethodNegotiator;
import org.springjutsu.validation.mvc.SuccessViewHandlerInterceptor;
import org.springjutsu.validation.mvc.ValidationFailureViewHandlerExceptionResolver;
import org.w3c.dom.Element;

public class ValidationMVCAnnotationsDefinitionParser implements BeanDefinitionParser {
	
	/**
	 * Do actual parsing.
	 */
	public BeanDefinition parse(Element configNode, ParserContext context) {
		
		BeanDefinitionBuilder controllerMethodNegotiatorBuilder =
			BeanDefinitionBuilder.genericBeanDefinition(ControllerMethodNegotiator.class);
		BeanDefinitionBuilder successViewHandlerBuilder = 
			BeanDefinitionBuilder.genericBeanDefinition(SuccessViewHandlerInterceptor.class);
		BeanDefinitionBuilder validationFailureViewHandlerBuilder = 
			BeanDefinitionBuilder.genericBeanDefinition(ValidationFailureViewHandlerExceptionResolver.class);
		BeanDefinitionBuilder successViewHandlerMapperBuilder = 
			BeanDefinitionBuilder.genericBeanDefinition(MappedInterceptor.class);
		
		// Register them beans.
		registerInfrastructureBean(configNode, context, controllerMethodNegotiatorBuilder);
		registerInfrastructureBean(configNode, context, validationFailureViewHandlerBuilder);
		String successViewBeanName = 
			registerInfrastructureBean(configNode, context, successViewHandlerBuilder);
		String[] successViewMappings = new String[]{"/**"};
		// Map success view handler
		successViewHandlerMapperBuilder.addConstructorArgValue(successViewMappings);
		successViewHandlerMapperBuilder.addConstructorArgReference(successViewBeanName);
		registerInfrastructureBean(configNode, context, successViewHandlerMapperBuilder);
		
		return null;
	}
	
	private String registerInfrastructureBean(Element element, ParserContext context, 
			BeanDefinitionBuilder componentBuilder) {
		
		BeanDefinition definition = componentBuilder.getBeanDefinition();
		String entityName = context.getReaderContext().registerWithGeneratedName(definition);
		context.registerComponent(new BeanComponentDefinition(definition, entityName));
		return entityName;
	}

}
