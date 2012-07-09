package org.springjutsu.validation.namespace;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springjutsu.validation.ValidationManager;
import org.springjutsu.validation.executors.RuleExecutorContainer;
import org.springjutsu.validation.executors.RuleExecutorContainer.RuleExecutorBeanRegistrant;
import org.springjutsu.validation.rules.ValidationRulesContainer;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Registers a @link{ValidationManager}, 
 * a @link{ValidationRulesContainer},
 * and a @link{ruleExecutorContainer}
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
		BeanDefinitionBuilder ruleExecutorContainerBuilder = 
			BeanDefinitionBuilder.genericBeanDefinition(RuleExecutorContainer.class);
		BeanDefinitionBuilder validationRulesContainerBuilder =
			BeanDefinitionBuilder.genericBeanDefinition(ValidationRulesContainer.class);
		
		// Parse message configuration...
		Element messageConfig = (Element) configNode.getElementsByTagNameNS(configNode.getNamespaceURI(), "message-config").item(0);
		if (messageConfig != null) {
			validationManagerBuilder.addPropertyValue("errorMessagePrefix", 
					messageConfig.getAttribute("errorMessagePrefix"));
			validationManagerBuilder.addPropertyValue("fieldLabelPrefix", 
					messageConfig.getAttribute("fieldLabelPrefix"));
			validationManagerBuilder.addPropertyValue("enableSuperclassFieldLabelLookup", 
					messageConfig.getAttribute("enableSuperclassFieldLabelLookup"));
		}
		
		// Parse rules configuration...
		Element rulesConfig = (Element) configNode.getElementsByTagNameNS(configNode.getNamespaceURI(), "rules-config").item(0);
		if (rulesConfig != null) {
			boolean addDefaultRules = Boolean.valueOf(rulesConfig.getAttribute("addDefaultRuleExecutors"));
			ruleExecutorContainerBuilder.addPropertyValue("addDefaultRuleExecutors", addDefaultRules);
			
			List<RuleExecutorBeanRegistrant> ruleExecutors = new ArrayList<RuleExecutorBeanRegistrant>();
			NodeList ruleExecutorNodes = rulesConfig.getElementsByTagNameNS(rulesConfig.getNamespaceURI(), "rule-executor");
			for (int executorNbr = 0; executorNbr < ruleExecutorNodes.getLength(); executorNbr++) {
				Element ruleExecutorNode = (Element) ruleExecutorNodes.item(executorNbr);
				BeanDefinitionBuilder executorBuilder = BeanDefinitionBuilder
					.genericBeanDefinition(ruleExecutorNode.getAttribute("class"));
				String ruleExecutorBeanName = registerInfrastructureBean(configNode, context, executorBuilder);
				ruleExecutors.add(new RuleExecutorBeanRegistrant(
					ruleExecutorBeanName, ruleExecutorNode.getAttribute("name")));
			}
			ruleExecutorContainerBuilder.addPropertyValue("ruleExecutorBeanRegistrants", ruleExecutors);
		}
		
		// Register them beans.
		registerInfrastructureBean(configNode, context, validationRulesContainerBuilder);
		registerInfrastructureBean(configNode, context, ruleExecutorContainerBuilder);
		context.registerBeanComponent(new BeanComponentDefinition(
			validationManagerBuilder.getBeanDefinition(), configNode.getAttribute("validatorName")));
		
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
