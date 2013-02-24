package org.springjutsu.validation.integrationTests;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.springframework.validation.Errors;
import org.springjutsu.validation.context.ValidationContextHandler;
import org.springjutsu.validation.spel.SPELResolver;
import org.springjutsu.validation.test.entities.Customer;

public class ContextIntegrationTest extends ValidationIntegrationTest {
	
	@Rule
	public static TestName TEST_NAME = new TestName();
	
	@Override
	protected String getXmlSubdirectory() {
		return "contextIntegrationTest";
	}
	
	@Test
	public void testCustomContextRuleEvaluation() {
		Customer customer = new Customer();
		Errors errors = doValidate("testCustomContextRuleEvaluation.xml", customer).errors;
		assertEquals(2, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("emailAddress").getCode());
		assertEquals("errors.required", errors.getFieldError("firstName").getCode());
	}
	
	@Test
	public void testCustomContextTemplateEvaluation() {
		Customer customer = new Customer();
		Errors errors = doValidate("testCustomContextTemplateEvaluation.xml", customer).errors;
		assertEquals(2, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("emailAddress").getCode());
		assertEquals("errors.required", errors.getFieldError("firstName").getCode());
	}
	
	@Test
	public void testCustomContextELEnrichment() {
		Customer customer = new Customer();
		Errors errors = doValidate("testCustomContextELEnrichment.xml", customer).errors;
		assertEquals(2, errors.getErrorCount());
		assertEquals("errors.matches", errors.getFieldError("emailAddress").getCode());
		assertEquals("errors.matches", errors.getFieldError("lastName").getCode());
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
		assertEquals("errors.required", errors.getFieldError("copayer.emailAddress").getCode());
		assertEquals("errors.required", errors.getFieldError("copayer.lastName").getCode());
	}
	
	@Test
	public void testAnnotationConfiguredCustomContextActivation() {
		Customer customer = new Customer();
		Errors errors = doValidate("testAnnotationConfiguredCustomContextActivation.xml", customer).errors;
		assertEquals(2, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("emailAddress").getCode());
		assertEquals("errors.required", errors.getFieldError("firstName").getCode());
	}
	
	public static class TestNameContextHandler implements ValidationContextHandler {

		@Override
		public boolean isActive(Set<String> qualifiers) {
			return ContextIntegrationTest.TEST_NAME != null
				&& ContextIntegrationTest.TEST_NAME.getMethodName() != null
				&& qualifiers.contains(ContextIntegrationTest.TEST_NAME.getMethodName());
		}

		@Override
		public boolean enableDuringSubBeanValidation() {
			return false;
		}

		@Override
		public void initializeSPELResolver(SPELResolver spelResolver) {
			spelResolver.getScopedContext().addContext("jUnitTestName", ContextIntegrationTest.TEST_NAME);
		}
		
	}
	
	public static class AlwaysActiveTestNameContextHandler extends TestNameContextHandler {

		@Override
		public boolean enableDuringSubBeanValidation() {
			return true;
		}
		
	}

}
