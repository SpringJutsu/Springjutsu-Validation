package org.springjutsu.validation.integrationTests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.validation.Errors;
import org.springjutsu.validation.test.entities.Customer;
import org.springjutsu.validation.test.entities.ValuedCustomer;

public class ValidationGroupsIntegrationTest extends ValidationIntegrationTest {
	
	@Override
	protected String getXmlSubdirectory() {
		return "validationGroupsIntegrationTest";
	}
	
	@Test
	public void testGroupActivationByString() {
		Customer customer = new Customer();
		Errors errors = doValidate("testGroupActivationByString.xml", customer, 
				new String[] {"matchingGroupName"}).errors;
		assertEquals(2, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("emailAddress").getCode());
		assertEquals("errors.required", errors.getFieldError("firstName").getCode());
	}
	
	@Test
	public void testGroupActivationByClass() {
		Customer customer = new Customer();
		Errors errors = doValidate("testGroupActivationByClass.xml", customer, 
				new Object[]{ValuedCustomer.class}).errors;
		assertEquals(2, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("emailAddress").getCode());
		assertEquals("errors.required", errors.getFieldError("firstName").getCode());
	}
	
	@Test
	public void testGroupActivationWithMultipleQualifiers() {
		Customer customer = new Customer();
		Errors errors = doValidate("testGroupActivationWithMultipleQualifiers.xml", customer, 
				new String[] {"matchingGroupName"}).errors;
		assertEquals(2, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("emailAddress").getCode());
		assertEquals("errors.required", errors.getFieldError("firstName").getCode());
	}

}
