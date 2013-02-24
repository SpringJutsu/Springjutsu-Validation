package org.springjutsu.validation.integrationTests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.validation.Errors;
import org.springjutsu.validation.exceptions.CircularValidationTemplateReferenceException;
import org.springjutsu.validation.exceptions.IllegalTemplateReferenceException;
import org.springjutsu.validation.test.entities.Address;
import org.springjutsu.validation.test.entities.Color;
import org.springjutsu.validation.test.entities.Customer;

public class ValidationTemplatesIntegrationTest extends ValidationIntegrationTest {
	
	@Override
	protected String getXmlSubdirectory() {
		return "validationTemplatesIntegrationTest";
	}
	
	@Test
	public void testValidationTemplates() {
		setCurrentFormPath("/foo/new");
		Customer customer = new Customer();
		Errors errors = doValidate("testValidationTemplates.xml", customer).errors;
		assertEquals(3, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("address.lineOne").getCode());
		assertEquals("errors.required", errors.getFieldError("address.city").getCode());
		assertEquals("errors.required", errors.getFieldError("secondaryAddress.city").getCode());
	}
	
	@Test(expected=CircularValidationTemplateReferenceException.class)
	public void testValidationTemplatesWithBasicIllegalRecursion() {
		setCurrentFormPath("/foo/new");
		Customer customer = new Customer();
		doValidate("testValidationTemplatesWithBasicIllegalRecursion.xml", customer);
	}
	
	@Test(expected=CircularValidationTemplateReferenceException.class)
	public void testValidationTemplatesWithComplexIllegalRecursion() {
		setCurrentFormPath("/foo/new");
		Customer customer = new Customer();
		customer.setFavoriteColor(Color.GREEN);
		customer.setAddress(new Address());
		customer.getAddress().setLineOne("555 Some road");
		customer.getAddress().setLineTwo("Fourth building on the left");
		customer.getAddress().setZipCode("55555");
		doValidate("testValidationTemplatesWithComplexIllegalRecursion.xml", customer);
	}
	
	@Test
	public void testValidationTemplatesWithNestedRulesInTemplate() {
		setCurrentFormPath("/foo/new");
		Customer customer = new Customer();
		customer.setAddress(new Address());
		customer.setSecondaryAddress(new Address());
		customer.getAddress().setLineTwo("not an empty line two");
		customer.getSecondaryAddress().setLineTwo("also not an empty line two.");
		Errors errors = doValidate("testValidationTemplatesWithNestedRulesInTemplate.xml", customer).errors;
		assertEquals(2, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("address.lineOne").getCode());
		assertEquals("errors.required", errors.getFieldError("secondaryAddress.lineOne").getCode());
	}
	
	@Test
	public void testValidationTemplatesWithRuleNestedTemplateUse() {
		setCurrentFormPath("/foo/new");
		Customer customer = new Customer();
		customer.setFirstName("bob");
		customer.setLastName("smith");
		customer.setAddress(new Address());
		customer.getAddress().setZipCode("123456");
		customer.setSecondaryAddress(new Address());
		customer.getSecondaryAddress().setZipCode("123456");
		
		Customer copayer = new Customer();
		copayer.setLastName("Anderson");
		copayer.setAddress(new Address());
		copayer.getAddress().setZipCode("123456");
		copayer.setSecondaryAddress(new Address());
		copayer.getSecondaryAddress().setZipCode("123456");
		
		Customer referrer = new Customer();
		referrer.setLastName("Rufus");
		referrer.setAddress(new Address());
		referrer.getAddress().setZipCode("123456");
		referrer.setSecondaryAddress(new Address());
		referrer.getSecondaryAddress().setZipCode("123456");
		
		customer.setCopayer(copayer);
		customer.setReferredBy(referrer);
		
		Errors errors = doValidate("testValidationTemplatesWithRuleNestedTemplateUse.xml", customer).errors;
		assertEquals(4, errors.getErrorCount());
		assertEquals("errors.exactLength", errors.getFieldError("secondaryAddress.zipCode").getCode());
		assertEquals("errors.exactLength", errors.getFieldError("copayer.secondaryAddress.zipCode").getCode());
		assertEquals("errors.exactLength", errors.getFieldError("address.zipCode").getCode());
		assertEquals("errors.exactLength", errors.getFieldError("referredBy.secondaryAddress.zipCode").getCode());
	}
	
	@Test
	public void testValidationTemplatesWithSimpleTemplateUse() {
		setCurrentFormPath("/foo/new");
		Customer customer = new Customer();
		customer.setAddress(new Address());
		customer.getAddress().setZipCode("123456");
		customer.setSecondaryAddress(new Address());
		customer.getSecondaryAddress().setZipCode("123456");
		
		Errors errors = doValidate("testValidationTemplatesWithSimpleTemplateUse.xml", customer).errors;
		assertEquals(2, errors.getErrorCount());
		assertEquals("errors.exactLength", errors.getFieldError("address.zipCode").getCode());
		assertEquals("errors.exactLength", errors.getFieldError("secondaryAddress.zipCode").getCode());
	}
	
	@Test
	public void testValidationTemplatesWithTemplateNestedTemplateUse() {
		setCurrentFormPath("/foo/new");
		Customer customer = new Customer();
		customer.setAddress(new Address());
		customer.getAddress().setZipCode("123456");
		customer.setSecondaryAddress(new Address());
		customer.getSecondaryAddress().setZipCode("123456");
		
		Customer copayer = new Customer();
		copayer.setAddress(new Address());
		copayer.getAddress().setZipCode("123456");
		copayer.setSecondaryAddress(new Address());
		copayer.getSecondaryAddress().setZipCode("123456");
		
		Customer referrer = new Customer();
		referrer.setAddress(new Address());
		referrer.getAddress().setZipCode("123456");
		referrer.setSecondaryAddress(new Address());
		referrer.getSecondaryAddress().setZipCode("123456");
		
		customer.setCopayer(copayer);
		customer.setReferredBy(referrer);
		
		Errors errors = doValidate("testValidationTemplatesWithTemplateNestedTemplateUse.xml", customer).errors;
		assertEquals(3, errors.getErrorCount());
		assertEquals("errors.exactLength", errors.getFieldError("address.zipCode").getCode());
		assertEquals("errors.exactLength", errors.getFieldError("referredBy.secondaryAddress.zipCode").getCode());
		assertEquals("errors.exactLength", errors.getFieldError("copayer.secondaryAddress.zipCode").getCode());
	}
	
	@Test(expected=IllegalTemplateReferenceException.class)
	public void testValidationTemplatesWithWrongClassForTemplate() {
		setCurrentFormPath("/foo/new");
		Customer customer = new Customer();
		customer.setFavoriteColor(Color.GREEN);
		doValidate("testValidationTemplatesWithWrongClassForTemplate.xml", customer);
	}

}
