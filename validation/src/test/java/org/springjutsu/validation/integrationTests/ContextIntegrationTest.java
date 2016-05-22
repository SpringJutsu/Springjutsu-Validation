package org.springjutsu.validation.integrationTests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.validation.Errors;
import org.springjutsu.validation.test.entities.Customer;

public class ContextIntegrationTest extends ValidationIntegrationTest {
	
	@Override
	protected String getXmlSubdirectory() {
		return "contextIntegrationTest";
	}
	
	@Test
	public void testCustomContextRuleEvaluation() {
		Customer customer = new Customer();
		Errors errors = doValidate("testCustomContextRuleEvaluation.xml", customer).errors;
		assertEquals(2, errors.getErrorCount());
		assertEquals("messageOverride.errors.required", errors.getFieldError("emailAddress").getCode());
		assertEquals("messageOverride.errors.required", errors.getFieldError("firstName").getCode());
	}
	
	@Test
	public void testCustomContextTemplateEvaluation() {
		Customer customer = new Customer();
		Errors errors = doValidate("testCustomContextTemplateEvaluation.xml", customer).errors;
		assertEquals(2, errors.getErrorCount());
		assertEquals("messageOverride.errors.required", errors.getFieldError("emailAddress").getCode());
		assertEquals("messageOverride.errors.required", errors.getFieldError("firstName").getCode());
	}
	
	@Test
	public void testCustomContextELEnrichment() {
		Customer customer = new Customer();
		Errors errors = doValidate("testCustomContextELEnrichment.xml", customer).errors;
		assertEquals(2, errors.getErrorCount());
		assertEquals("messageOverride.errors.matches", errors.getFieldError("emailAddress").getCode());
		assertEquals("messageOverride.errors.matches", errors.getFieldError("lastName").getCode());
	}
	
	@Test
	public void testCustomContextActivationDuringRecursiveValidation() {
		Customer customer = new Customer();
		customer.setFirstName("Bob");
		customer.setLastName("Bob");
		customer.setEmailAddress("bob@bob.bob");
		customer.setCopayer(new Customer());
		Errors errors = doValidate("testCustomContextActivationDuringRecursiveValidation.xml", customer).errors;
		assertEquals(2, errors.getErrorCount());
		assertEquals("messageOverride.errors.required", errors.getFieldError("copayer.emailAddress").getCode());
		assertEquals("messageOverride.errors.required", errors.getFieldError("copayer.lastName").getCode());
	}
	
	@Test
	public void testAnnotationConfiguredCustomContextActivation() {
		Customer customer = new Customer();
		Errors errors = doValidate("testAnnotationConfiguredCustomContextActivation.xml", customer).errors;
		assertEquals(2, errors.getErrorCount());
		assertEquals("messageOverride.errors.required", errors.getFieldError("emailAddress").getCode());
		assertEquals("messageOverride.errors.required", errors.getFieldError("firstName").getCode());
	}
	
	public static class AlwaysActiveTestNameContextHandler extends TestNameContextHandler {

		@Override
		public boolean enableDuringSubBeanValidation() {
			return true;
		}
		
	}

}
