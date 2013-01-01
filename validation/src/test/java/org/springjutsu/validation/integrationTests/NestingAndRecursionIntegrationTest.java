package org.springjutsu.validation.integrationTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.springframework.validation.Errors;
import org.springjutsu.validation.test.entities.Address;
import org.springjutsu.validation.test.entities.Customer;

public class NestingAndRecursionIntegrationTest extends ValidationIntegrationTest {
	
	@Test
	public void testNestedRules() {
		Customer customer = new Customer();
		customer.setFirstName("bob");
		Errors errors = doValidate("testNestedRules.xml", customer).errors;
		assertEquals(2, errors.getErrorCount());
		assertNull(errors.getFieldError("firstName"));
		assertEquals("errors.required", errors.getFieldError("lastName").getCode());
		assertEquals("errors.required", errors.getFieldError("emailAddress").getCode());
	}
	
	@Test
	public void testPreventRuleRecursion() {
		Customer customer = new Customer();
		customer.setReferredBy(customer);
		Address address = new Address();
		address.setCustomer(customer);
		customer.setAddress(address);
		customer.setSecondaryAddress(address);
		
		Errors errors = doValidate("testPreventRuleRecursion.xml", customer).errors;
		assertEquals(3, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("firstName").getCode());
		assertNull(errors.getFieldError("referredBy.firstName"));
		assertEquals("errors.required", errors.getFieldError("address.city").getCode());
		assertNull(errors.getFieldError("address.customer.firstName"));
		assertEquals("errors.required", errors.getFieldError("secondaryAddress.state").getCode());
		assertNull(errors.getFieldError("secondaryAddress.customer.firstName"));
	}

}
