package org.springjutsu.validation.executors.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springjutsu.validation.executors.RuleExecutor;

public class RequiredRuleExecutorTest {

	@Test
	public void testValidate() throws Exception {
		RuleExecutor executor = new RequiredRuleExecutor();
		assertTrue(executor.validate(123, null));
		assertTrue(executor.validate("test", null));
		assertFalse(executor.validate("", null));
		assertFalse(executor.validate(null, null));		
	}
}
