package org.springjutsu.validation.namespace;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springjutsu.validation.ValidationErrorMessageHandler;
import org.springjutsu.validation.ValidationManager;
import org.springjutsu.validation.context.ValidationContextHandlerContainer;
import org.springjutsu.validation.executors.RuleExecutorContainer;
import org.springjutsu.validation.rules.RecursiveValidationExclude;
import org.springjutsu.validation.rules.RecursiveValidationInclude;
import org.springjutsu.validation.rules.ValidationRulesContainer;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Registers a @link{ValidationManager}, 
 * a @link{ValidationRulesContainer},
 * a @link{RuleExecutorContainer},
 * and a @link{ValidationContextHandlerContainer}
 * with the user-specified configuration.
 * @author Clark Duplichien
 */
public class ValidationConfigurationDefinitionParser implements BeanDefinitionParser {
	
	/**
	 * Do actual parsing.
	 */
	public BeanDefinition parse(Element configNode, ParserContext context) {
		
		BeanDefinitionBuilder validationManagerBuilder = 
			BeanDefinitionBuilder.genericBeanDefinition(ValidationManager.class);
		BeanDefinitionBuilder validationErrorMessageHandlerBuilder = 
			BeanDefinitionBuilder.genericBeanDefinition(ValidationErrorMessageHandler.class);
		BeanDefinitionBuilder ruleExecutorContainerBuilder = 
			BeanDefinitionBuilder.genericBeanDefinition(RuleExecutorContainer.class);
		BeanDefinitionBuilder contextHandlerContainerBuilder = 
			BeanDefinitionBuilder.genericBeanDefinition(ValidationContextHandlerContainer.class);
		BeanDefinitionBuilder validationRulesContainerBuilder =
			BeanDefinitionBuilder.genericBeanDefinition(ValidationRulesContainer.class);
		
		// Parse message configuration...
		Element messageConfig = (Element) configNode.getElementsByTagNameNS(configNode.getNamespaceURI(), "message-config").item(0);
		if (messageConfig != null) {
			validationErrorMessageHandlerBuilder.addPropertyValue("errorMessagePrefix", 
					messageConfig.getAttribute("errorMessagePrefix"));
			validationErrorMessageHandlerBuilder.addPropertyValue("fieldLabelPrefix", 
					messageConfig.getAttribute("fieldLabelPrefix"));
			validationErrorMessageHandlerBuilder.addPropertyValue("enableSuperclassFieldLabelLookup", 
					messageConfig.getAttribute("enableSuperclassFieldLabelLookup"));
		}
		
		// Parse rules configuration...
		Element rulesConfig = (Element) configNode.getElementsByTagNameNS(configNode.getNamespaceURI(), "rules-config").item(0);
		if (rulesConfig != null) {
			boolean addDefaultRules = Boolean.valueOf(rulesConfig.getAttribute("addDefaultRuleExecutors"));
			ruleExecutorContainerBuilder.addPropertyValue("addDefaultRuleExecutors", addDefaultRules);
			
			List<KeyedBeanRegistrant> ruleExecutors = new ArrayList<KeyedBeanRegistrant>();
			NodeList ruleExecutorNodes = rulesConfig.getElementsByTagNameNS(rulesConfig.getNamespaceURI(), "rule-executor");
			for (int executorNbr = 0; executorNbr < ruleExecutorNodes.getLength(); executorNbr++) {
				Element ruleExecutorNode = (Element) ruleExecutorNodes.item(executorNbr);
				BeanDefinitionBuilder executorBuilder = BeanDefinitionBuilder
					.genericBeanDefinition(ruleExecutorNode.getAttribute("class"));
				String ruleExecutorBeanName = registerInfrastructureBean(context, executorBuilder);
				ruleExecutors.add(new KeyedBeanRegistrant(ruleExecutorBeanName, ruleExecutorNode.getAttribute("name")));
			}
			ruleExecutorContainerBuilder.addPropertyValue("ruleExecutorBeanRegistrants", ruleExecutors);
			
			List<Class<?>> excludeAnnotations = new ArrayList<Class<?>>();
			excludeAnnotations.add(RecursiveValidationExclude.class);
			
			NodeList excludeAnnotationNodes = rulesConfig.getElementsByTagNameNS(rulesConfig.getNamespaceURI(), "recursion-exclude-annotation");
			for (int excludeNbr = 0; excludeNbr < excludeAnnotationNodes.getLength(); excludeNbr++) {
				Element excludeNode = (Element) excludeAnnotationNodes.item(excludeNbr);
				String excludeAnnotationClass = excludeNode.getAttribute("class");
				try {
					excludeAnnotations.add(Class.forName(excludeAnnotationClass));
				} catch (ClassNotFoundException cnfe) {
					throw new IllegalArgumentException("Invalid exclude annotation class: " + excludeAnnotationClass, cnfe);
				}
			}
			validationRulesContainerBuilder.addPropertyValue("excludeAnnotations", excludeAnnotations);
			
			List<Class<?>> includeAnnotations = new ArrayList<Class<?>>();
			includeAnnotations.add(RecursiveValidationInclude.class);
			
			NodeList includeAnnotationNodes = rulesConfig.getElementsByTagNameNS(rulesConfig.getNamespaceURI(), "recursion-include-annotation");
			for (int includeNbr = 0; includeNbr < includeAnnotationNodes.getLength(); includeNbr++) {
				Element includeNode = (Element) includeAnnotationNodes.item(includeNbr);
				String includeAnnotationClass = includeNode.getAttribute("class");
				try {
					includeAnnotations.add(Class.forName(includeAnnotationClass));
				} catch (ClassNotFoundException cnfe) {
					throw new IllegalArgumentException("Invalid include annotation class: " + includeAnnotationClass, cnfe);
				}
			}
			validationRulesContainerBuilder.addPropertyValue("includeAnnotations", includeAnnotations);
		}
		
		// Parse context configuration...
		Element contextConfig = (Element) configNode.getElementsByTagNameNS(configNode.getNamespaceURI(), "context-config").item(0);
		if (contextConfig != null) {
			boolean addDefaultContextHandlers = Boolean.valueOf(contextConfig.getAttribute("addDefaultContextHandlers"));
			contextHandlerContainerBuilder.addPropertyValue("addDefaultContextHandlers", addDefaultContextHandlers);
			
			List<KeyedBeanRegistrant> contextHandlers = new ArrayList<KeyedBeanRegistrant>();
			NodeList contextHandlerNodes = contextConfig.getElementsByTagNameNS(contextConfig.getNamespaceURI(), "context-handler");
			for (int handlerNbr = 0; handlerNbr < contextHandlerNodes.getLength(); handlerNbr++) {
				Element contextHandlerNode = (Element) contextHandlerNodes.item(handlerNbr);
				BeanDefinitionBuilder handlerBuilder = BeanDefinitionBuilder
					.genericBeanDefinition(contextHandlerNode.getAttribute("class"));
				String ruleExecutorBeanName = registerInfrastructureBean(context, handlerBuilder);
				contextHandlers.add(new KeyedBeanRegistrant(ruleExecutorBeanName, contextHandlerNode.getAttribute("type")));
			}
			contextHandlerContainerBuilder.addPropertyValue("contextHandlerBeanRegistrants", contextHandlers);
		}
		
		// Register them beans.
		registerInfrastructureBean(context, validationRulesContainerBuilder);
		registerInfrastructureBean(context, ruleExecutorContainerBuilder);
		registerInfrastructureBean(context, contextHandlerContainerBuilder);
		registerInfrastructureBean(context, validationErrorMessageHandlerBuilder);
		context.registerBeanComponent(new BeanComponentDefinition(
			validationManagerBuilder.getBeanDefinition(), configNode.getAttribute("validatorName")));
		
		return null;
	}
	
	private String registerInfrastructureBean(ParserContext context, BeanDefinitionBuilder componentBuilder) {
		BeanDefinition definition = componentBuilder.getBeanDefinition();
		String entityName = context.getReaderContext().registerWithGeneratedName(definition);
		context.registerComponent(new BeanComponentDefinition(definition, entityName));
		return entityName;
	}

}
