package org.springjutsu.validation.integrationTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.springframework.validation.Errors;
import org.springjutsu.validation.test.entities.Customer;

public class NestingIntegrationTest extends ValidationIntegrationTest {
	
	@Override
	protected String getXmlSubdirectory() {
		return "nestingIntegrationTest";
	}
	
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
	public void testEverythingNestedUnderRule() {
		Customer customer = new Customer();
		customer.setEmailAddress("bob@bob.bob");
		setCurrentFormPath("/foo/1/edit");
		Errors errors = doValidate("testEverythingNestedUnderRule.xml", customer, 
				new String[] {"matchingGroupName"}).errors;
		assertEquals(5, errors.getErrorCount());
		assertEquals(5, errors.getFieldErrorCount("firstName"));
	}
	
	@Test
	public void testEverythingNestedUnderTemplateDefinition() {
		Customer customer = new Customer();
		customer.setEmailAddress("bob@bob.bob");
		setCurrentFormPath("/foo/1/edit");
		Errors errors = doValidate("testEverythingNestedUnderTemplateDefinition.xml", customer, 
				new String[] {"matchingGroupName"}).errors;
		assertEquals(5, errors.getErrorCount());
		assertEquals(5, errors.getFieldErrorCount("firstName"));
	}
	
	@Test
	public void testEverythingNestedUnderForm() {
		Customer customer = new Customer();
		customer.setEmailAddress("bob@bob.bob");
		setCurrentFormPath("/foo/1/edit");
		Errors errors = doValidate("testEverythingNestedUnderForm.xml", customer, 
				new String[] {"matchingGroupName"}).errors;
		assertEquals(5, errors.getErrorCount());
		assertEquals(5, errors.getFieldErrorCount("firstName"));
	}
	
	@Test
	public void testEverythingNestedUnderGroup() {
		Customer customer = new Customer();
		customer.setEmailAddress("bob@bob.bob");
		setCurrentFormPath("/foo/1/edit");
		Errors errors = doValidate("testEverythingNestedUnderGroup.xml", customer, 
				new String[] {"matchingGroupName"}).errors;
		assertEquals(5, errors.getErrorCount());
		assertEquals(5, errors.getFieldErrorCount("firstName"));
	}
	
	@Test
	public void testEverythingNestedUnderContext() {
		Customer customer = new Customer();
		customer.setEmailAddress("bob@bob.bob");
		setCurrentFormPath("/foo/1/edit");
		Errors errors = doValidate("testEverythingNestedUnderContext.xml", customer, 
				new String[] {"matchingGroupName"}).errors;
		assertEquals(5, errors.getErrorCount());
		assertEquals(5, errors.getFieldErrorCount("firstName"));
	}
	
	@Test
	public void testNoNestedRulesButOnFailSkipChildren() {
		Customer customer = new Customer();
		Errors errors = doValidate("testNoNestedRulesButOnFailSkipChildren.xml", customer).errors;
		assertEquals(0, errors.getErrorCount());
	}
	
	@Test
	public void testNoNestedRulesButOnFailError() {
		Customer customer = new Customer();
		Errors errors = doValidate("testNoNestedRulesButOnFailError.xml", customer).errors;
		assertEquals(1, errors.getErrorCount());
		assertEquals(1, errors.getFieldErrorCount("firstName"));
	}
	
	@Test
	public void testNestedRulesAndOnFailSkipChildren() {
		Customer customer = new Customer();
		Errors errors = doValidate("testNestedRulesAndOnFailSkipChildren.xml", customer).errors;
		assertEquals(0, errors.getErrorCount());
	}
	
	@Test
	public void testNestedRulesAndOnFailError() {
		Customer customer = new Customer();
		Errors errors = doValidate("testNestedRulesAndOnFailError.xml", customer).errors;
		assertEquals(1, errors.getErrorCount());
		assertEquals(1, errors.getFieldErrorCount("firstName"));
	}
	
	@Test
	public void testNestedRulesAndOnFailErrorWithoutFail() {
		Customer customer = new Customer();
		customer.setFirstName("bob");
		Errors errors = doValidate("testNestedRulesAndOnFailError.xml", customer).errors;
		assertEquals(1, errors.getErrorCount());
		assertEquals(1, errors.getFieldErrorCount("emailAddress"));
	}

}
