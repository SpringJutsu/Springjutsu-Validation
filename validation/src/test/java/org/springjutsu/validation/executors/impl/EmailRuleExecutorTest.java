package org.springjutsu.validation.executors.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EmailRuleExecutorTest {

	@Test
	public void testValidate() throws Exception {
		EmailRuleExecutor executor = new EmailRuleExecutor();
		assertTrue(executor.validate(null, null));
		assertTrue(executor.validate("test@test.com", null));
		assertTrue(executor.validate("test_test@test.net", null));
		assertTrue(executor.validate("test.test@test.edu", null));
		assertFalse(executor.validate("t@est@test.com", null));
		assertFalse(executor.validate("te###st@test.com", null));
	}

}
