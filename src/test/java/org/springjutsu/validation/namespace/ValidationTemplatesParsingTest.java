package org.springjutsu.validation.namespace;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springjutsu.validation.exceptions.CircularValidationTemplateReferenceException;
import org.springjutsu.validation.rules.ValidationEntity;
import org.springjutsu.validation.rules.ValidationRulesContainer;
import org.springjutsu.validation.test.entities.Customer;

public class ValidationTemplatesParsingTest {
	
	private static final String xmlDirectory = 
		"org/springjutsu/validation/namespace/";
	
	protected ValidationEntity triggerValidationParse(ApplicationContext context) 
		throws CircularValidationTemplateReferenceException {
		ValidationRulesContainer rulesContainer = 
			context.getBean(ValidationRulesContainer.class);
		return rulesContainer.getValidationEntity(Customer.class);
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
	public void testIllegalModelRuleTemplates() {
	
	}
	
	/** TODO */
	/*
	@Test
	public void testTemplateClassMismatch() {
		ApplicationContext context =
		    new ClassPathXmlApplicationContext(new String[] {
		    	xmlDirectory + "validationTemplates-wrongClassForTemplate-config.xml"});
		triggerValidationParse(context);
	}
	*/
	
	@Test
	public void testUnwrapSimpleTemplateUse() {
		ApplicationContext context =
		    new ClassPathXmlApplicationContext(new String[] {
		    	xmlDirectory + "validationTemplates-simpleTemplateUse-config.xml"});
		ValidationEntity entity = triggerValidationParse(context);
		Assert.assertEquals(1, entity.getContextValidationRules("mockFlow:mockState").size());
		Assert.assertEquals("address.zipCode", entity.getContextValidationRules("mockFlow:mockState").get(0).getPath());
	}
	
	@Test
	public void testUnwrapRuleNestedTemplateUse() {
		ApplicationContext context =
		    new ClassPathXmlApplicationContext(new String[] {
		    	xmlDirectory + "validationTemplates-ruleNestedTemplateUse-config.xml"});
		ValidationEntity entity = triggerValidationParse(context);
		Assert.assertEquals(1, entity.getContextValidationRules("mockFlow:mockState").size());
		Assert.assertEquals("firstName", entity.getContextValidationRules("mockFlow:mockState").get(0).getPath());
		Assert.assertEquals(2, entity.getContextValidationRules("mockFlow:mockState").get(0).getRules().size());
		Assert.assertEquals("address.zipCode", entity.getContextValidationRules("mockFlow:mockState").get(0).getRules().get(0).getPath());
		Assert.assertEquals("referredBy.lastName", entity.getContextValidationRules("mockFlow:mockState").get(0).getRules().get(1).getPath());
		Assert.assertEquals(1, entity.getContextValidationRules("mockFlow:mockState").get(0).getRules().get(1).getRules().size());
		Assert.assertEquals("referredBy.secondaryAddress.zipCode", entity.getContextValidationRules("mockFlow:mockState").get(0).getRules().get(1).getRules().get(0).getPath());
	}

}
