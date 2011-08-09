package org.springjutsu.validation.namespace;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.annotation.ExpectedException;
import org.springjutsu.validation.exceptions.CircularValidationTemplateReferenceException;
import org.springjutsu.validation.rules.ValidationRulesContainer;
import org.springjutsu.validation.test.entities.Customer;

public class ValidationTemplatesParsingTest {
	
	private static final String xmlDirectory = 
		"org/springjutsu/validation/namespace/";
	
	protected void triggerValidationParse(ApplicationContext context) 
		throws CircularValidationTemplateReferenceException {
		ValidationRulesContainer rulesContainer = 
			context.getBean(ValidationRulesContainer.class);
		rulesContainer.getValidationEntity(Customer.class);
	}
	
	@Test(expected=CircularValidationTemplateReferenceException.class)
	public void testBasicIllegalRecursion() {
		ApplicationContext context =
		    new ClassPathXmlApplicationContext(new String[] {
		    	xmlDirectory + "validationTemplates-basicIllegalRecursion-config.xml"});
		triggerValidationParse(context);
	}
	
	@Test(expected=CircularValidationTemplateReferenceException.class)
	public void testComplexIllegalRecursion() {
		ApplicationContext context =
		    new ClassPathXmlApplicationContext(new String[] {
		    	xmlDirectory + "validationTemplates-complexIllegalRecursion-config.xml"});
		triggerValidationParse(context);
	}
	
	/** TODO */
	@Test
	public void testSimpleTemplateUse() {
		/*
		ApplicationContext context =
		    new ClassPathXmlApplicationContext(new String[] {
		    	xmlDirectory + "validationTemplates-simpleTemplateUse-config.xml"});
		triggerValidationParse(context);
		*/
	}

}
