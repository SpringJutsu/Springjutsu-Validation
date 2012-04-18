/*
 * Copyright 2010-2011 Duplichien, Wicksell, Springjutsu.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springjutsu.validation.namespace;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springjutsu.validation.mvc.SuccessViewHandlerInterceptor;
import org.springjutsu.validation.mvc.ValidationFailureViewHandlerExceptionResolver;
import org.w3c.dom.Element;

/**
 * Parses the validation namespace "mvc-annotations" element
 * into all the beans and registrations required in order to configure
 * use of the @link{SuccessView} and @link{ValidationFailureView} annotations. 
 * @author Clark Duplichien
 *
 */
public class ValidationMVCAnnotationsDefinitionParser implements BeanDefinitionParser {
	
	/**
	 * Do actual parsing.
	 */
	public BeanDefinition parse(Element configNode, ParserContext context) {
		
		BeanDefinitionBuilder successViewHandlerBuilder = 
			BeanDefinitionBuilder.genericBeanDefinition(SuccessViewHandlerInterceptor.class);
		BeanDefinitionBuilder validationFailureViewHandlerBuilder = 
			BeanDefinitionBuilder.genericBeanDefinition(ValidationFailureViewHandlerExceptionResolver.class);
		BeanDefinitionBuilder successViewHandlerMapperBuilder = 
			BeanDefinitionBuilder.genericBeanDefinition(MappedInterceptor.class);
		
		// Register them beans.
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
