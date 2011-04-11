package org.springjutsu.validation.executors.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springjutsu.validation.executors.RuleExecutor;

public class NumericRuleExecutorTest {

	@Test
	public void testValidate() throws Exception {
		RuleExecutor executor = new NumericRuleExecutor();
		assertTrue(executor.validate(null, null));
		assertTrue(executor.validate("123", null));
		assertTrue(executor.validate(123, null));
		assertTrue(executor.validate(new Integer(123), null));
		assertFalse(executor.validate("abc", null));
		assertFalse(executor.validate("JohnJoeBobSmith", null));
		assertFalse(executor.validate("c4ke", null));
		assertFalse(executor.validate("test-123", null));
	}

}
