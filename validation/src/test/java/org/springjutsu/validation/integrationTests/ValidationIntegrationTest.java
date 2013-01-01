package org.springjutsu.validation.integrationTests;

import org.junit.After;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.validation.Errors;
import org.springframework.web.context.request.RequestContextHolder;
import org.springjutsu.validation.ValidationManager;

public abstract class ValidationIntegrationTest {
	
	protected static final String xmlDirectory = 
		"org/springjutsu/validation/integration/";
	
	protected TestResult doValidate(String configXml, Object validateMe) {
		ApplicationContext context =
		    new ClassPathXmlApplicationContext(new String[] {
		    	xmlDirectory + configXml});
		ValidationManager manager = context.getBean(ValidationManager.class);
		return new TestResult(manager.validate(validateMe), context.getBean(MessageSource.class));
	}
	
	protected class TestResult {
		public Errors errors;
		public MessageSource messageSource;
		public TestResult(Errors errors, MessageSource messageSource) {
			this.errors = errors;
			this.messageSource = messageSource;
		}
	}
	
	@After
	public void cleanupRequests() {
		RequestContextHolder.setRequestAttributes(null);
		org.springframework.webflow.execution.RequestContextHolder.setRequestContext(null);
	}	
	
}
