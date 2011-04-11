package org.springjutsu.validation.executors.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springjutsu.validation.executors.RuleExecutor;

public class AlphabeticRuleExecutorTest {

	@Test
	public void testValidate() throws Exception {
		RuleExecutor executor = new AlphabeticRuleExecutor();
		assertTrue(executor.validate("abc", null));
		assertTrue(executor.validate("JohnJoeBobSmith", null));
		assertFalse(executor.validate("123", null));
		assertFalse(executor.validate("c4ke", null));
	}

}
