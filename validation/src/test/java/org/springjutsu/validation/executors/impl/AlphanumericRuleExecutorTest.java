package org.springjutsu.validation.executors.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AlphanumericRuleExecutorTest {

	@Test
	public void testValidate() throws Exception {
		AlphanumericRuleExecutor executor = new AlphanumericRuleExecutor();
		assertTrue(executor.validate(null, null));
		assertTrue(executor.validate("abc", null));
		assertTrue(executor.validate("JohnJoeBobSmith", null));
		assertTrue(executor.validate("123", null));
		assertTrue(executor.validate("c4ke", null));
		assertFalse(executor.validate("test-123", null));
	}

}
