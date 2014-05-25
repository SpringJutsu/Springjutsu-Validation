package org.springjutsu.validation.dsl;

import org.junit.Assert;
import org.junit.Test;
import org.springjutsu.validation.dsl.Validation;
import org.springjutsu.validation.rules.ValidationEntity;
import org.springjutsu.validation.test.entities.Customer;
import org.springjutsu.validation.test.entities.Person;

public class ValidationRuleBuilderTest {

	@Test
	public void testBasicRuleBuilder()
	{
		ValidationEntity entity = Validation.forEntity(Person.class)
				.havingRules(
						Validation.rule("firstName", "notNull").withMessage("blam yo"),
						Validation.rule("lastName", "notNull").withMessage("dizzam")
						)
				.build();
		
		Assert.assertEquals(Person.class, entity.getValidationClass());
		Assert.assertEquals("firstName", entity.getRules().get(0).getPath());
		Assert.assertEquals("notNull", entity.getRules().get(0).getType());
		Assert.assertEquals("blam yo", entity.getRules().get(0).getMessage());
		Assert.assertEquals("lastName", entity.getRules().get(1).getPath());
		Assert.assertEquals("notNull", entity.getRules().get(1).getType());
		Assert.assertEquals("dizzam", entity.getRules().get(1).getMessage());
	}
	
	@Test
	public void testNestedRules()
	{
		ValidationEntity entity = Validation.forEntity(Customer.class)
				.havingRules(
						Validation.rule("firstName", "notNull").withMessage("blam yo"),
						Validation.rule("lastName", "notNull").withMessage("dizzam"),
						Validation.rule("address", "notNull").withMessage("doh").havingRules(
							Validation.rule("lineOne", "notNull").withMessage("p-p-p-pow")
								)
						)
				.build();
		
		Assert.assertEquals(Customer.class, entity.getValidationClass());
		Assert.assertEquals("firstName", entity.getRules().get(0).getPath());
		Assert.assertEquals("notNull", entity.getRules().get(0).getType());
		Assert.assertEquals("blam yo", entity.getRules().get(0).getMessage());
		Assert.assertEquals("lastName", entity.getRules().get(1).getPath());
		Assert.assertEquals("notNull", entity.getRules().get(1).getType());
		Assert.assertEquals("dizzam", entity.getRules().get(1).getMessage());
		Assert.assertEquals("address", entity.getRules().get(2).getPath());
		Assert.assertEquals("notNull", entity.getRules().get(2).getType());
		Assert.assertEquals("doh", entity.getRules().get(2).getMessage());
		Assert.assertEquals("lineOne", entity.getRules().get(2).getRules().get(0).getPath());
		Assert.assertEquals("notNull", entity.getRules().get(2).getRules().get(0).getType());
		Assert.assertEquals("p-p-p-pow", entity.getRules().get(2).getRules().get(0).getMessage());
	}
	
	@Test
	public void testContextsAndGroups()
	{
		ValidationEntity entity = Validation.forEntity(Customer.class)
				.havingValidationContexts(Validation.context("group", "dave")
				.havingRules(
						Validation.rule("firstName", "notNull").withMessage("blam yo"),
						Validation.rule("lastName", "notNull").withMessage("dizzam"),
						Validation.rule("address", "notNull").withMessage("doh")
						.havingValidationContexts(Validation.group("buster")
							.havingRules(Validation.rule("lineOne", "notNull").withMessage("p-p-p-pow")))
						))
				.build();

		Assert.assertEquals(Customer.class, entity.getValidationClass());
		Assert.assertEquals("group", entity.getValidationContexts().get(0).getType());
		Assert.assertEquals("dave", entity.getValidationContexts().get(0).getQualifiers().toArray()[0]);
		Assert.assertEquals("firstName", entity.getValidationContexts().get(0).getRules().get(0).getPath());
		Assert.assertEquals("notNull", entity.getValidationContexts().get(0).getRules().get(0).getType());
		Assert.assertEquals("blam yo", entity.getValidationContexts().get(0).getRules().get(0).getMessage());
		Assert.assertEquals("lastName", entity.getValidationContexts().get(0).getRules().get(1).getPath());
		Assert.assertEquals("notNull", entity.getValidationContexts().get(0).getRules().get(1).getType());
		Assert.assertEquals("dizzam", entity.getValidationContexts().get(0).getRules().get(1).getMessage());
		Assert.assertEquals("address", entity.getValidationContexts().get(0).getRules().get(2).getPath());
		Assert.assertEquals("notNull", entity.getValidationContexts().get(0).getRules().get(2).getType());
		Assert.assertEquals("doh", entity.getValidationContexts().get(0).getRules().get(2).getMessage());

		Assert.assertEquals("group", entity.getValidationContexts().get(0).getRules().get(2).getValidationContexts().get(0).getType());
		Assert.assertEquals("buster", entity.getValidationContexts().get(0).getRules().get(2).getValidationContexts().get(0).getQualifiers().toArray()[0]);
		Assert.assertEquals("lineOne", entity.getValidationContexts().get(0).getRules().get(2).getValidationContexts().get(0).getRules().get(0).getPath());
		Assert.assertEquals("notNull", entity.getValidationContexts().get(0).getRules().get(2).getValidationContexts().get(0).getRules().get(0).getType());
		Assert.assertEquals("p-p-p-pow", entity.getValidationContexts().get(0).getRules().get(2).getValidationContexts().get(0).getRules().get(0).getMessage());	
	}

}





