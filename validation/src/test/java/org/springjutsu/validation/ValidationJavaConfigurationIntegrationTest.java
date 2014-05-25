package org.springjutsu.validation;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.validation.Errors;
import org.springjutsu.validation.dsl.Validation;
import org.springjutsu.validation.rules.ValidationEntity;
import org.springjutsu.validation.test.entities.Address;
import org.springjutsu.validation.test.entities.Customer;
import org.springjutsu.validation.test.entities.Person;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=ValidationJavaConfigurationIntegrationTestConfig.class)
public class ValidationJavaConfigurationIntegrationTest {

	@Autowired
	ValidationManager validationManager;
	
	@Test
	public void testSimpleCase() {
		Customer customer = new Customer();
		Errors errors = validationManager.validate(customer);
		Assert.assertEquals(1, errors.getErrorCount());
		Assert.assertEquals("favoriteColor required", errors.getFieldErrors("favoriteColor").get(0).getDefaultMessage());
		
	}
	
	@Test
	public void testWithContext()
	{
		Customer customer = new Customer();
		Errors errors = validationManager.validate(customer, "dave");
		Assert.assertEquals(4, errors.getErrorCount());
		Assert.assertEquals("errors.required", errors.getFieldErrors("favoriteColor").get(0).getCode());
		Assert.assertEquals("blam", errors.getFieldErrors("firstName").get(0).getCode());
		Assert.assertEquals("dizzam", errors.getFieldErrors("lastName").get(0).getCode());
		Assert.assertEquals("doh", errors.getFieldErrors("address").get(0).getCode());
		Assert.assertTrue(errors.getFieldErrors("address.lineOne").isEmpty());
		
		customer.setAddress(new Address());
		errors = validationManager.validate(customer, "dave", "buster");
		Assert.assertEquals(4, errors.getErrorCount());
		Assert.assertFalse(errors.getFieldErrors("address.lineOne").isEmpty());
		Assert.assertEquals("p-p-p-pow", errors.getFieldErrors("address.lineOne").get(0).getCode());

	}

}

@Configurable
@EnableValidation
class ValidationJavaConfigurationIntegrationTestConfig
{
	
	@Bean
	ValidationEntity personValidation()
	{
		return Validation.forEntity(Customer.class)
				.havingRules(Validation.rule("favoriteColor", "required"))
				.havingValidationContexts(Validation.context("group", "dave")
					.havingRules(
							Validation.rule("firstName", "required").withMessage("blam"),
							Validation.rule("lastName", "required").withMessage("dizzam"),
							Validation.rule("address", "required").withMessage("doh")
							.havingValidationContexts(Validation.group("buster")
								.havingRules(Validation.rule("address.lineOne", "required").withMessage("p-p-p-pow")))
							))
				.build();

	}
}