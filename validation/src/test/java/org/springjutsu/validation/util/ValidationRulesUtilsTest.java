package org.springjutsu.validation.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ValidationRulesUtilsTest {

	@Test
	public void testGetLength() {
		assertEquals(0, ValidationRulesUtils.getLength(""));
		assertEquals(1, ValidationRulesUtils.getLength("a"));
		assertEquals(5, ValidationRulesUtils.getLength("abcde"));
		assertEquals(1, ValidationRulesUtils.getLength(1));
		assertEquals(5, ValidationRulesUtils.getLength(12345));
	}

	@Test
	public void testIsEmpty() {
		assertTrue(ValidationRulesUtils.isEmpty(null));
		assertTrue(ValidationRulesUtils.isEmpty(""));
		assertFalse(ValidationRulesUtils.isEmpty("123"));
	}
}
