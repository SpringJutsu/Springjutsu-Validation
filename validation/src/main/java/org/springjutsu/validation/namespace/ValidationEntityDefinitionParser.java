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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.springjutsu.validation.rules.CollectionStrategy;
import org.springjutsu.validation.rules.ValidationContext;
import org.springjutsu.validation.rules.ValidationEntity;
import org.springjutsu.validation.rules.ValidationRule;
import org.springjutsu.validation.rules.ValidationTemplate;
import org.springjutsu.validation.rules.ValidationTemplateReference;
import org.springjutsu.validation.util.PathUtils;
import org.springjutsu.validation.util.RequestUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
/**
 * Parses XML from the validation namespace into a @link{ValidationEntity}
 * object to be stored in the @link{ValidationEntityContainer}
 * @author Clark Duplichien
 * @author Taylor Wicksell
 *
 */
public class ValidationEntityDefinitionParser implements BeanDefinitionParser {

	/**
	 * Do actual parsing.
	 * Since rules may be nested, delegate to {@link #parseNestedRules(Element, Class)}
	 * where necessary.
	 */
	public BeanDefinition parse(Element entityNode, ParserContext parserContext) {
		
		RootBeanDefinition entityDefinition = new RootBeanDefinition(ValidationEntity.class);
		
		String className = entityNode.getAttribute("class");
		Class<?> modelClass;
		try {
			modelClass = Class.forName(className);
		} catch (ClassNotFoundException cnfe) {
			throw new ValidationParseException("Class " + className + " does not exist as a model class.");
		}
		
		List<String> excludePaths = new ArrayList<String>();		
		NodeList excludes = entityNode.getElementsByTagNameNS(
				entityNode.getNamespaceURI(), "recursion-exclude");
		for (int excludeNbr = 0; excludeNbr < excludes.getLength(); excludeNbr++) {
			Element excludeNode = (Element) excludes.item(excludeNbr);
			String path = excludeNode.getAttribute("propertyName");
			if (path.contains(".")) {
				throw new ValidationParseException("Invalid recursion-exclude property name \"" + path 
					+ "\" Exclude paths should not be nested fields.");
			} else {
				excludePaths.add(path);
			}
		}
		
		List<String> includePaths = new ArrayList<String>();
		NodeList includes = entityNode.getElementsByTagNameNS(
				entityNode.getNamespaceURI(), "recursion-include");
		for (int includeNbr = 0; includeNbr < includes.getLength(); includeNbr++) {
			Element includeNode = (Element) includes.item(includeNbr);
			String path = includeNode.getAttribute("propertyName");
			if (path.contains(".")) {
				throw new ValidationParseException("Invalid recursion-include property name \"" + path 
					+ "\" Include paths should not be nested fields.");
			} else {
				includePaths.add(path);
			}
		}
		
		ValidationStructure validationStructure = parseNestedValidation(entityNode, modelClass);
		
		List<ValidationTemplate> templates = new ArrayList<ValidationTemplate>();
		NodeList templateNodes = entityNode.getElementsByTagNameNS(
				entityNode.getNamespaceURI(), "template");
		for (int templateNbr = 0; templateNbr < templateNodes.getLength(); templateNbr++) {
			Element templateNode = (Element) templateNodes.item(templateNbr);
			String templateName = templateNode.getAttribute("name");
			ValidationStructure templateValidation = parseNestedValidation(templateNode, modelClass);
			ValidationTemplate template = new ValidationTemplate(templateName, modelClass);
			template.setRules(templateValidation.rules);
			template.setTemplateReferences(templateValidation.refs);
			templates.add(template);
		}
		
		entityDefinition.getPropertyValues().add("rules", validationStructure.rules);
		entityDefinition.getPropertyValues().add("templateReferences", validationStructure.refs);
		entityDefinition.getPropertyValues().add("validationContexts", validationStructure.contexts);
		entityDefinition.getPropertyValues().add("validationTemplates", templates);
		entityDefinition.getPropertyValues().add("validationClass", modelClass);
		entityDefinition.getPropertyValues().add("includedPaths", includePaths);
		entityDefinition.getPropertyValues().add("excludedPaths", excludePaths);
		String entityName = parserContext.getReaderContext().registerWithGeneratedName(entityDefinition);
		parserContext.registerComponent(new BeanComponentDefinition(entityDefinition, entityName));
		return null;
	}
	
	/**
	 * Parses nested validation rule and template-ref structures
	 * @param ruleNode the ValidationRule node
	 * @param modelClass the model class
	 * @return a @link{ValidationStructure} object whose
	 *  nested rules and template references are those
	 *  rules and template references stemming from the ruleNode.
	 */
	protected ValidationStructure parseNestedValidation(Element ruleNode, Class<?> modelClass) {
		
		ValidationStructure structure = new ValidationStructure();
		
		if (ruleNode == null) {
			return structure;
		}

		List<Element> validationRuleNodes = DomUtils.getChildElementsByTagName(ruleNode, "rule");
		
		if (validationRuleNodes != null) {
			for (Element rule : validationRuleNodes) {
				String path = rule.getAttribute("path");
				if (path != null && path.length() > 0 
						&& !path.contains("${") 
						&& !PathUtils.pathExists(modelClass, path)) {
					throw new ValidationParseException("Path \"" + path 
							+ "\" does not exist on class " + modelClass.getCanonicalName());
				}
				String type = rule.getAttribute("type");
				String value = rule.getAttribute("value");
				String message = rule.getAttribute("message");
				String errorPath = rule.getAttribute("errorPath");
				String collectionStrategy = rule.getAttribute("collectionStrategy");
				ValidationRule validationRule = new ValidationRule(path, type, value);
				validationRule.setMessage(message);
				validationRule.setErrorPath(errorPath);
				validationRule.setCollectionStrategy(CollectionStrategy.forXmlValue(collectionStrategy));
				ValidationStructure subStructure = parseNestedValidation(rule, modelClass);
				validationRule.setRules(subStructure.rules);
				validationRule.setTemplateReferences(subStructure.refs);
				validationRule.setValidationContexts(subStructure.contexts);
				structure.rules.add(validationRule);
			}
		}
		
		List<Element> validationTemplateReferenceNodes = 
			DomUtils.getChildElementsByTagName(ruleNode, "template-ref");
		
		if (validationTemplateReferenceNodes != null) {
			for (Element templateReference : validationTemplateReferenceNodes) {
				String basePath = templateReference.getAttribute("basePath");
				if (basePath != null && basePath.length() > 0 
						&& !basePath.contains("${") 
						&& !PathUtils.pathExists(modelClass, basePath)) {
					throw new ValidationParseException("Path \"" + basePath 
							+ "\" does not exist on class " + modelClass.getCanonicalName());
				}
				String templateName = templateReference.getAttribute("templateName");
				ValidationTemplateReference templateRef = 
					new ValidationTemplateReference(basePath, templateName);
				structure.refs.add(templateRef);
			}
		}
		
		List<Element> formNodes = DomUtils.getChildElementsByTagName(ruleNode, "form");
		if (formNodes != null) {
			for (Element formNode : formNodes) {
				
				// get form paths.
				String formPaths = formNode.getAttribute("path");
				Set<String> formConstraints = new HashSet<String>();
				for (String formPath : formPaths.split(",")) {
					String candidateFormPath = formPath.trim();
					candidateFormPath = RequestUtils.removeLeadingAndTrailingSlashes(candidateFormPath).trim();
					if (!candidateFormPath.isEmpty()) {
						formConstraints.add(candidateFormPath);
					}
				}
				
				ValidationContext formContext = new ValidationContext();
				formContext.setType("form");
				
				ValidationContext flowContext = new ValidationContext();
				flowContext.setType("webflow");

				for (String formConstraint : formConstraints) {
					if (formConstraint.contains(":")) {
						flowContext.getQualifiers().add(formConstraint);
					} else {
						formContext.getQualifiers().add(formConstraint);
					}
				}
				
				// get rules & templates.
				ValidationStructure formSpecificValidationStructure = parseNestedValidation(formNode, modelClass);
				formContext.setRules(formSpecificValidationStructure.rules);
				formContext.setTemplateReferences(formSpecificValidationStructure.refs);
				formContext.setValidationContexts(formSpecificValidationStructure.contexts);
				flowContext.setRules(formSpecificValidationStructure.rules);
				flowContext.setTemplateReferences(formSpecificValidationStructure.refs);
				flowContext.setValidationContexts(formSpecificValidationStructure.contexts);
				
				if (!formContext.getQualifiers().isEmpty()) {
					structure.contexts.add(formContext);
				}
				if (!flowContext.getQualifiers().isEmpty()) {
					structure.contexts.add(flowContext);
				}
			}
		}
		
		List<Element> groupNodes = DomUtils.getChildElementsByTagName(ruleNode, "group");
		if (groupNodes != null) {
			for (Element groupNode : groupNodes) {
				
				// get form paths.
				String groupQualifiers = groupNode.getAttribute("qualifiers");
				Set<String> groupConstraints = new HashSet<String>();
				for (String qualifier : groupQualifiers.split(",")) {
					groupConstraints.add(qualifier.trim());
				}
				
				ValidationContext groupContext = new ValidationContext();
				groupContext.setType("group");
				groupContext.setQualifiers(groupConstraints);
				
				// get rules & templates.
				ValidationStructure groupSpecificValidationStructure = parseNestedValidation(groupNode, modelClass);
				groupContext.setRules(groupSpecificValidationStructure.rules);
				groupContext.setTemplateReferences(groupSpecificValidationStructure.refs);
				groupContext.setValidationContexts(groupSpecificValidationStructure.contexts);
				structure.contexts.add(groupContext);
			}
		}
		
		List<Element> contextNodes = DomUtils.getChildElementsByTagName(ruleNode, "context");
		if (contextNodes != null) {
			for (Element contextNode : contextNodes) {
				
				// get form paths.
				String contextQualifiers = contextNode.getAttribute("qualifiers");
				Set<String> contextConstraints = new HashSet<String>();
				for (String qualifier : contextQualifiers.split(",")) {
					contextConstraints.add(qualifier.trim());
				}
				
				ValidationContext context = new ValidationContext();
				context.setType(contextNode.getAttribute("type"));
				context.setQualifiers(contextConstraints);
				
				// get rules & templates.
				ValidationStructure contextSpecificValidationStructure = parseNestedValidation(contextNode, modelClass);
				context.setRules(contextSpecificValidationStructure.rules);
				context.setTemplateReferences(contextSpecificValidationStructure.refs);
				context.setValidationContexts(contextSpecificValidationStructure.contexts);
				structure.contexts.add(context);
			}
		}
		
		return structure;
	}
	
	/**
	 * Exception when validation cannot be parsed.
	 * @author Clark Duplichien
	 * @author Taylor Wicksell
	 *
	 */
	public class ValidationParseException extends RuntimeException {
		
		private static final long serialVersionUID = 1L;

		public ValidationParseException() {
			super();		
		}
		
		public ValidationParseException(String message) {
			super(message);
		}
	}
	
	protected class ValidationStructure {
		public List<ValidationRule> rules = new ArrayList<ValidationRule>();
		public List<ValidationTemplateReference> refs = new ArrayList<ValidationTemplateReference>();
		public List<ValidationContext> contexts = new ArrayList<ValidationContext>();
	}
}

