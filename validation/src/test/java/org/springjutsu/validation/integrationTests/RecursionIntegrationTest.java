package org.springjutsu.validation.integrationTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.springframework.validation.Errors;
import org.springjutsu.validation.test.entities.Address;
import org.springjutsu.validation.test.entities.Customer;
import org.springjutsu.validation.test.entities.ExtendedLessSkippablePerson;
import org.springjutsu.validation.test.entities.ExtendedSkippablePerson;
import org.springjutsu.validation.test.entities.LessSkippablePerson;
import org.springjutsu.validation.test.entities.SkippablePerson;

public class RecursionIntegrationTest extends ValidationIntegrationTest {
	
	@Override
	protected String getXmlSubdirectory() {
		return "recursionIntegrationTest";
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
	
	@Test
	public void testIgnoreExcludedSubPaths() {
		SkippablePerson skippablePerson = new SkippablePerson();
		skippablePerson.setSkipMe(new SkippablePerson());
		skippablePerson.setSkipMeFromXml(new SkippablePerson());
		skippablePerson.setCustomSkipMe(new SkippablePerson());
		skippablePerson.setDontSkipMeFromXml(new SkippablePerson());
		skippablePerson.getSkipUs().add(new SkippablePerson());
		skippablePerson.getSkipUsFromXml().add(new SkippablePerson());
		skippablePerson.getDontSkipUsFromXml().add(new SkippablePerson());
		
		Errors errors = doValidate("testRecursionExclusions.xml", skippablePerson).errors;
		assertEquals(3, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("name").getCode());
		assertEquals("errors.required", errors.getFieldError("dontSkipMeFromXml.name").getCode());
		assertEquals("errors.required", errors.getFieldError("dontSkipUsFromXml[0].name").getCode());
	}
	
	@Test
	public void testIgnorePolymorphicExcludedSubPaths() {
		SkippablePerson skippablePerson = new ExtendedSkippablePerson();
		skippablePerson.setSkipMe(new SkippablePerson());
		skippablePerson.setSkipMeFromXml(new SkippablePerson());
		skippablePerson.setCustomSkipMe(new SkippablePerson());
		skippablePerson.setDontSkipMeFromXml(new SkippablePerson());
		skippablePerson.getSkipUs().add(new SkippablePerson());
		skippablePerson.getSkipUsFromXml().add(new SkippablePerson());
		skippablePerson.getDontSkipUsFromXml().add(new SkippablePerson());
		
		Errors errors = doValidate("testPolymorphicRecursionExclusions.xml", skippablePerson).errors;
		assertEquals(3, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("name").getCode());
		assertEquals("errors.required", errors.getFieldError("dontSkipMeFromXml.name").getCode());
		assertEquals("errors.required", errors.getFieldError("dontSkipUsFromXml[0].name").getCode());
	}
	
	@Test
	public void testValidateIncludedSubPaths() {
		LessSkippablePerson lessSkippablePerson = new LessSkippablePerson();
		lessSkippablePerson.setSkipMe(new LessSkippablePerson());
		lessSkippablePerson.setSkipMeFromXml(new LessSkippablePerson());
		lessSkippablePerson.setCustomSkipMe(new LessSkippablePerson());
		lessSkippablePerson.setDontSkipMe(new LessSkippablePerson());
		lessSkippablePerson.setDontSkipMeFromXml(new LessSkippablePerson());
		lessSkippablePerson.setCustomDontSkipMe(new LessSkippablePerson());
		lessSkippablePerson.getSkipUs().add(new LessSkippablePerson());
		lessSkippablePerson.getSkipUsFromXml().add(new LessSkippablePerson());
		lessSkippablePerson.getDontSkipUs().add(new LessSkippablePerson());
		lessSkippablePerson.getDontSkipUsFromXml().add(new LessSkippablePerson());
		lessSkippablePerson.getCustomDontSkipUs().add(new LessSkippablePerson());
		
		Errors errors = doValidate("testRecursionInclusions.xml", lessSkippablePerson).errors;
		assertEquals(7, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("name").getCode());
		assertEquals("errors.required", errors.getFieldError("dontSkipMe.name").getCode());
		assertEquals("errors.required", errors.getFieldError("dontSkipMeFromXml.name").getCode());
		assertEquals("errors.required", errors.getFieldError("customDontSkipMe.name").getCode());
		assertEquals("errors.required", errors.getFieldError("dontSkipUs[0].name").getCode());
		assertEquals("errors.required", errors.getFieldError("dontSkipUsFromXml[0].name").getCode());
		assertEquals("errors.required", errors.getFieldError("customDontSkipUs[0].name").getCode());
	}
	
	@Test
	public void testValidatePolymorphicIncludedSubPaths() {
		LessSkippablePerson lessSkippablePerson = new ExtendedLessSkippablePerson();
		lessSkippablePerson.setSkipMe(new LessSkippablePerson());
		lessSkippablePerson.setSkipMeFromXml(new LessSkippablePerson());
		lessSkippablePerson.setCustomSkipMe(new LessSkippablePerson());
		lessSkippablePerson.setDontSkipMe(new LessSkippablePerson());
		lessSkippablePerson.setDontSkipMeFromXml(new LessSkippablePerson());
		lessSkippablePerson.setCustomDontSkipMe(new LessSkippablePerson());
		lessSkippablePerson.getSkipUs().add(new LessSkippablePerson());
		lessSkippablePerson.getSkipUsFromXml().add(new LessSkippablePerson());
		lessSkippablePerson.getDontSkipUs().add(new LessSkippablePerson());
		lessSkippablePerson.getDontSkipUsFromXml().add(new LessSkippablePerson());
		lessSkippablePerson.getCustomDontSkipUs().add(new LessSkippablePerson());
		
		Errors errors = doValidate("testPolymorphicRecursionInclusions.xml", lessSkippablePerson).errors;
		assertEquals(7, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("name").getCode());
		assertEquals("errors.required", errors.getFieldError("dontSkipMe.name").getCode());
		assertEquals("errors.required", errors.getFieldError("dontSkipMeFromXml.name").getCode());
		assertEquals("errors.required", errors.getFieldError("customDontSkipMe.name").getCode());
		assertEquals("errors.required", errors.getFieldError("dontSkipUs[0].name").getCode());
		assertEquals("errors.required", errors.getFieldError("dontSkipUsFromXml[0].name").getCode());
		assertEquals("errors.required", errors.getFieldError("customDontSkipUs[0].name").getCode());
	}

}
