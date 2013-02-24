package org.springjutsu.validation.integrationTests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.validation.Errors;
import org.springjutsu.validation.rules.ValidationRulesContainer;
import org.springjutsu.validation.test.entities.Customer;
import org.springjutsu.validation.test.entities.Developer;
import org.springjutsu.validation.test.entities.Person;
import org.springjutsu.validation.test.entities.ValuedCustomer;

public class PolymorphismIntegrationTest extends ValidationIntegrationTest {
	
	@Override
	protected String getXmlSubdirectory() {
		return "polymorphismIntegrationTest";
	}
	
	@Test
	public void testRuleInheritance() {
		ApplicationContext context =
		    new ClassPathXmlApplicationContext(new String[] {
		    	xmlDirectory + "polymorphismIntegrationTest/testInheritance.xml"});
		ValidationRulesContainer container = context.getBean(ValidationRulesContainer.class);
		assertEquals(1, container.getValidationEntity(Person.class).getRules().size());
		assertEquals(1, container.getValidationEntity(Person.class).getValidationContexts().size());
		assertEquals(2, container.getValidationEntity(Developer.class).getRules().size());
		assertEquals(1, container.getValidationEntity(Developer.class).getValidationContexts().size());
		assertEquals(2, container.getValidationEntity(Customer.class).getRules().size());
		assertEquals(1, container.getValidationEntity(Customer.class).getValidationContexts().size());
		assertEquals(3, container.getValidationEntity(ValuedCustomer.class).getRules().size());
		assertEquals(1, container.getValidationEntity(ValuedCustomer.class).getValidationContexts().size());
	}
	
	/**
	 * Test fix for bug #14
	 */
	public void testInitializationDelegation() {
		ApplicationContext context =
		    new ClassPathXmlApplicationContext(new String[] {
		    	xmlDirectory + "testInheritance.xml"});
		ValidationRulesContainer container = context.getBean(ValidationRulesContainer.class);
		container.supportsClass(ValuedCustomer.class);
		assertEquals(4, container.getValidationEntity(ValuedCustomer.class).getRules().size());
	}
	
	/**
	 * Test fix for bug #14
	 */
	public void testInitializationDelegationAgain() {
		ApplicationContext context =
		    new ClassPathXmlApplicationContext(new String[] {
		    	xmlDirectory + "testInheritance.xml"});
		ValidationRulesContainer container = context.getBean(ValidationRulesContainer.class);
		container.hasRulesForClass(ValuedCustomer.class);
		assertEquals(4, container.getValidationEntity(ValuedCustomer.class).getRules().size());
	}
	
	@Test
	public void testPolymorphicMessageNegotiation() {
		ValuedCustomer customer = new ValuedCustomer();
		customer.setFirstName("bob");
		customer.setLastName("joe");
		TestResult result = doValidate("testPolymorphicMessageNegotiation.xml", customer);
		Errors errors = result.errors;
		MessageSource messageSource = result.messageSource;
		assertEquals(3, errors.getErrorCount());
		assertEquals("First Name must match Most Valuable Last Name", messageSource.getMessage(errors.getFieldError("firstName"), Locale.US));
		assertEquals("Most Valuable Last Name must be at least 4 characters long", messageSource.getMessage(errors.getFieldError("lastName"), Locale.US));
		assertEquals("emailAddress required", messageSource.getMessage(errors.getFieldError("emailAddress"), Locale.US));
		assertArrayEquals(new Object[]{"valuedCustomer.emailAddress"}, ((MessageSourceResolvable) errors.getFieldError("emailAddress").getArguments()[0]).getCodes());
	}
	
	@Test
	public void testPolymorphicMessageNegotiationDisabled() {
		ValuedCustomer customer = new ValuedCustomer();
		customer.setFirstName("bob");
		customer.setLastName("joe");
		TestResult result = doValidate("testPolymorphicMessageNegotiationDisabled.xml", customer);
		Errors errors = result.errors;
		MessageSource messageSource = result.messageSource;
		assertEquals(3, errors.getErrorCount());
		assertEquals("valuedCustomer.firstName must match Most Valuable Last Name", messageSource.getMessage(errors.getFieldError("firstName"), Locale.US));
		assertEquals("Most Valuable Last Name must be at least 4 characters long", messageSource.getMessage(errors.getFieldError("lastName"), Locale.US));
		assertEquals("emailAddress required", messageSource.getMessage(errors.getFieldError("emailAddress"), Locale.US));
		assertArrayEquals(new Object[]{"valuedCustomer.emailAddress"}, ((MessageSourceResolvable) errors.getFieldError("emailAddress").getArguments()[0]).getCodes());
	}
}
