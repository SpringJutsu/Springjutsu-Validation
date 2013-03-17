package org.springjutsu.validation.executors.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MinLengthRuleExecutorTest {

	@Test
	public void testValidate() throws Exception {
		MinLengthRuleExecutor executor = new MinLengthRuleExecutor();
		assertTrue(executor.validate(null, null));
		assertFalse(executor.validate("test", 5));
		assertFalse(executor.validate("1", 2));
		assertFalse(executor.validate(":)", 3));
		assertFalse(executor.validate(1234, 5));

		assertTrue(executor.validate("test", 4));
		assertTrue(executor.validate("1", 0));
		assertTrue(executor.validate(":)", 1));
		assertTrue(executor.validate(1234, 3));

	}

}
