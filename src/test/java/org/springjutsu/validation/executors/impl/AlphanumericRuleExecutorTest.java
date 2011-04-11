package org.springjutsu.validation.executors.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springjutsu.validation.executors.RuleExecutor;

public class AlphanumericRuleExecutorTest {

	@Test
	public void testValidate() throws Exception {
		RuleExecutor executor = new AlphanumericRuleExecutor();
		assertTrue(executor.validate(null, null));
		assertTrue(executor.validate("abc", null));
		assertTrue(executor.validate("JohnJoeBobSmith", null));
		assertTrue(executor.validate("123", null));
		assertTrue(executor.validate("c4ke", null));
		assertFalse(executor.validate("test-123", null));
	}

}
