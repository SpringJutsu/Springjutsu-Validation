package org.springjutsu.validation.namespace;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.webflow.core.collection.LocalParameterMap;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;
import org.springjutsu.validation.ValidationManager;
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
	
	protected Errors triggerValidationRun(ApplicationContext context, Object target, Map<String, String> requestData) {
		ValidationManager manager = 
			context.getBean(ValidationManager.class);
		MockRequestContext requestContext = new MockRequestContext(new LocalParameterMap(requestData));
		RequestContextHolder.setRequestContext(requestContext);
		Errors errors = new BeanPropertyBindingResult(target, "target");
		manager.validate(target, errors);
		return errors;
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
		ApplicationContext context =
		    new ClassPathXmlApplicationContext(new String[] {
		    	xmlDirectory + "validationTemplates-simpleTemplateUse-config.xml"});
		triggerValidationParse(context);
		Customer target = new Customer();
		Map<String, String> requestData = new HashMap<String, String>();
		requestData.put("firstName", "1234567");
		target.setFirstName("1234567");
		Errors errors = triggerValidationRun(context, target, requestData);
		Assert.assertEquals(1, errors.getErrorCount());
		Assert.assertEquals(1, errors.getFieldErrorCount("firstName"));
	}

}
