package org.springjutsu.validation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springjutsu.validation.context.ValidationContextHandlerContainer;
import org.springjutsu.validation.executors.RuleExecutorContainer;
import org.springjutsu.validation.rules.RecursiveValidationExclude;
import org.springjutsu.validation.rules.RecursiveValidationInclude;
import org.springjutsu.validation.rules.ValidationRulesContainer;

@Configuration
public class ValidationConfiguration implements ImportAware {

		protected AnnotationAttributes enableValidation;
		
		@Override
		public void setImportMetadata(AnnotationMetadata importMetadata) {
			this.enableValidation = AnnotationAttributes.fromMap(
					importMetadata.getAnnotationAttributes(EnableValidation.class.getName(), false));
			
		}
		
		@Bean
		public ValidationManager validationManager()
		{
			ValidationManager validationManager = new ValidationManager();
			return validationManager;
		}
		
		@Bean
		public ValidationErrorMessageHandler validationErrorMessageHandler()
		{
			ValidationErrorMessageHandler handler = new ValidationErrorMessageHandler();
			handler.setErrorMessagePrefix(enableValidation.getString("errorMessagePrefix"));
			handler.setFieldLabelPrefix(enableValidation.getString("fieldLabelPrefix"));
			handler.setEnableSuperclassFieldLabelLookup(enableValidation.getBoolean("enableSuperclassFieldLabelLookup"));
			return handler;
		}
		
		@Bean
		public RuleExecutorContainer ruleExecutorContainer()
		{
			RuleExecutorContainer ruleExecutorContainer = new RuleExecutorContainer();
			ruleExecutorContainer.setAddDefaultRuleExecutors(enableValidation.getBoolean("addDefaultRuleExecutors"));
			return ruleExecutorContainer;
		}
		
		
		@Bean
		public ValidationContextHandlerContainer validationContextHandlerContainer()
		{
			ValidationContextHandlerContainer validationContextHandlerContainer = new ValidationContextHandlerContainer();
			validationContextHandlerContainer.setAddDefaultContextHandlers(enableValidation.getBoolean("addDefaultContextHandlers"));
			return validationContextHandlerContainer;
		}
		
		@Bean
		public ValidationRulesContainer validationRulesContainer()
		{
			ValidationRulesContainer validationRulesContainer = new ValidationRulesContainer();
			List<Class<?>> excludeAnnotations = new ArrayList<Class<?>>();
			excludeAnnotations.add(RecursiveValidationExclude.class);
			validationRulesContainer.setExcludeAnnotations(excludeAnnotations);
			List<Class<?>> includeAnnotations = new ArrayList<Class<?>>();
			includeAnnotations.add(RecursiveValidationInclude.class);
			validationRulesContainer.setIncludeAnnotations(includeAnnotations);
			return validationRulesContainer;
		}
}