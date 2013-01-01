package org.springjutsu.validation.integrationTests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.validation.Errors;
import org.springjutsu.validation.test.entities.Customer;

public class ELIntegrationTest extends ValidationIntegrationTest {
	
	@Test
	public void testSPELvsModelBean() {
		Customer customer = new Customer();
		customer.setFirstName("bob");
		customer.setLastName("notBob");
		Errors errors = doValidate("testSPELvsModelBean.xml", customer).errors;
		assertEquals(2, errors.getErrorCount());
		assertEquals("errors.matches", errors.getFieldError("firstName").getCode());
		assertEquals("errors.matches", errors.getFieldError("lastName").getCode());
	}
	
	@Test(expected=IllegalStateException.class)
	public void testErrorPathRequiredForELModel() {
		Customer customer = new Customer();
		customer.setFirstName("cat");
		
		doValidate("testErrorPathRequiredForELModel.xml", customer);
	}
	
	/**
	 * Fix for #26
	 */
	@Test
	public void testELPathNotMutatedDuringNestedValidation() {
		Customer customer = new Customer();
		customer.setFirstName("bob");
		customer.setLastName("notBob");
		
		Customer customerTwo = new Customer();
		customerTwo.setFirstName("bob");
		customerTwo.setLastName("bob");
		customer.setReferredBy(customerTwo);
		
		Errors errors = doValidate("testELPathsNotMutated.xml", customer).errors;
		assertEquals(2, errors.getErrorCount());
		assertEquals("errors.matches", errors.getFieldError("firstName").getCode());
		assertEquals("errors.matches", errors.getFieldError("referredBy.firstName").getCode());
	}
	
	/**
	 * Fix for #26
	 */
	@Test
	public void testELPathNotMutatedDuringTemplateValidation() {
		Customer customer = new Customer();
		customer.setFirstName("bob");
		customer.setLastName("notBob");
		
		Customer customerTwo = new Customer();
		customerTwo.setFirstName("bob");
		customerTwo.setLastName("bob");
		customer.setReferredBy(customerTwo);
		
		Errors errors = doValidate("testELPathsNotMutatedInTemplates.xml", customer).errors;
		assertEquals(2, errors.getErrorCount());
		assertEquals("errors.matches", errors.getFieldError("firstName").getCode());
		assertEquals("errors.matches", errors.getFieldError("referredBy.firstName").getCode());
	}

}
