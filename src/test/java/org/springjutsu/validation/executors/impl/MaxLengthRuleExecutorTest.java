package org.springjutsu.validation.executors.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springjutsu.validation.executors.RuleExecutor;

public class MaxLengthRuleExecutorTest {

	@Test
	public void testValidate() throws Exception {
		RuleExecutor executor = new MaxLengthRuleExecutor();
		assertTrue(executor.validate(null, null));
		assertTrue(executor.validate("test", 4));
		assertTrue(executor.validate("1", 1));
		assertTrue(executor.validate(":)", 3));
		assertTrue(executor.validate(1234, 4));

		assertFalse(executor.validate("test", 3));
		assertFalse(executor.validate("1", 0));
		assertFalse(executor.validate(":)", 1));
		assertFalse(executor.validate(1234, 3));

	}

}
