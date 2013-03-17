package org.springjutsu.validation.executors.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ExactLengthRuleExecutorTest {

	@Test
	public void testValidate() throws Exception {
		ExactLengthRuleExecutor executor = new ExactLengthRuleExecutor();
		assertTrue(executor.validate(null, null));
		assertTrue(executor.validate("test", 4));
		assertTrue(executor.validate("1", 1));
		assertTrue(executor.validate(":)", 2));
		assertTrue(executor.validate(1234, 4));

		assertFalse(executor.validate("test", 3));
		assertFalse(executor.validate("1", 0));
		assertFalse(executor.validate(":)", 3));
		assertFalse(executor.validate(1234, 5));

	}

}
