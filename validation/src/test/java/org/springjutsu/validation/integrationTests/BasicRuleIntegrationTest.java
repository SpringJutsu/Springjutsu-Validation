package org.springjutsu.validation.integrationTests;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springjutsu.validation.test.entities.Customer;

public class BasicRuleIntegrationTest extends ValidationIntegrationTest {
	
	@Override
	protected String getXmlSubdirectory() {
		return "basicRuleIntegrationTest";
	}
	
	@Test
	public void testBasicRules() {
		Errors errors = doValidate("testBasicRules.xml", new Customer()).errors;
		assertEquals(3, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("firstName").getCode());
		assertEquals("errors.required", errors.getFieldError("lastName").getCode());
		assertEquals("errors.required", errors.getFieldError("emailAddress").getCode());
	}
	
	@Test
	public void testDefaultMessageNegotiation() {
		Customer customer = new Customer();
		customer.setFirstName("bob");
		customer.setLastName("joe");
		TestResult result = doValidate("testDefaultMessageNegotiation.xml", customer);
		Errors errors = result.errors;
		MessageSource messageSource = result.messageSource;
		assertEquals(3, errors.getErrorCount());
		assertEquals("First Name must match Last Name", messageSource.getMessage(errors.getFieldError("firstName"), Locale.US));
		assertEquals("Last Name must be at least 4 characters long", messageSource.getMessage(errors.getFieldError("lastName"), Locale.US));
		assertEquals("emailAddress required", messageSource.getMessage(errors.getFieldError("emailAddress"), Locale.US));
	}
	
	@Test
	public void testCustomMessageNegotiation() {
		Customer customer = new Customer();
		customer.setFirstName("bob");
		customer.setLastName("joe");
		TestResult result = doValidate("testCustomMessageNegotiation.xml", customer);
		Errors errors = result.errors;
		MessageSource messageSource = result.messageSource;
		assertEquals(3, errors.getErrorCount());
		assertEquals("First Name must match Last Name", messageSource.getMessage(errors.getFieldError("firstName"), Locale.US));
		assertEquals("Last Name must be at least 4 characters long", messageSource.getMessage(errors.getFieldError("lastName"), Locale.US));
		assertEquals("emailAddress required", messageSource.getMessage(errors.getFieldError("emailAddress"), Locale.US));
	}
	
	@Test
	public void testRuleWithNoPath() {
		Customer customer = new Customer();
		Errors errors = doValidate("testRuleWithNoPath.xml", customer).errors;
		assertEquals(1, errors.getErrorCount());
		assertEquals("errors.alphabetic", errors.getGlobalError().getCode());
	}
	
	@Test
	public void testProxiedRuleExecutor() {
		Customer customer = new Customer();
		customer.setFirstName("123456");
		Errors errors = doValidate("testProxiedRuleExecutor.xml", customer).errors;
		assertEquals(1, errors.getErrorCount());
		assertEquals("errors.proxyMe", errors.getFieldError("firstName").getCode());
	}

}
