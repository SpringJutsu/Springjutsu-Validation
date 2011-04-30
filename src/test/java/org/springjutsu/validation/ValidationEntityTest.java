package org.springjutsu.validation;

import junit.framework.Assert;

import org.junit.Test;
import org.springjutsu.validation.rules.ValidationRule;

public class ValidationEntityTest {
	
	ValidationRule successRule = new ValidationRule("success", "success", "success");
	ValidationRule failRule = new ValidationRule("fail", "fail", "fail");
	
	@Test
	public void testGetContextValidationRulesBasic() {
		ValidationEntity entity = new ValidationEntity();
		entity.addContextValidationRule("/account/new", successRule);
		Assert.assertEquals(successRule, entity.getContextValidationRules("/account/new").get(0));
	}
	
	@Test
	public void testGetContextValidationRulesRestVariable() {
		ValidationEntity entity = new ValidationEntity();
		entity.addContextValidationRule("/account/{id}/edit", successRule);
		Assert.assertEquals(successRule, entity.getContextValidationRules("/account/5/edit").get(0));
	}
	
	@Test
	public void testGetContextValidationRulesAntPath() {
		ValidationEntity entity = new ValidationEntity();
		entity.addContextValidationRule("/*/{id}/edit", successRule);
		Assert.assertEquals(successRule, entity.getContextValidationRules("/account/5/edit").get(0));
	}
	
	@Test
	public void testGetContextValidationRulesNestedAntPath() {
		ValidationEntity entity = new ValidationEntity();
		entity.addContextValidationRule("/**/{id}/edit", successRule);
		Assert.assertEquals(successRule, entity.getContextValidationRules("/account/financial/5/edit").get(0));
	}

}
