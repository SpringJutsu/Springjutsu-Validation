package org.springjutsu.validation.executors.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MatchesRuleExecutorTest {

	@Test
	public void testValidate() throws Exception {
		MatchesRuleExecutor executor = new MatchesRuleExecutor();
		assertTrue(executor.validate(null, null));
		assertTrue(executor.validate("Dave", "Dave"));
		assertTrue(executor.validate("124124", "124124"));
		assertFalse(executor.validate("Dave", "dave"));
		assertTrue(executor.validate(123, "123"));
		assertFalse(executor.validate("124124", "124123"));
		assertFalse(executor.validate("Dave", "Dive"));
		assertFalse(executor.validate("124124", "124-124"));
		assertFalse(executor.validate(123, "1234"));
		assertFalse(executor.validate("", "1234"));
		
	}

}
