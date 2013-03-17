package org.springjutsu.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.TypeMismatchException;
import org.springjutsu.validation.executors.impl.MatchesRuleExecutor;
import org.springjutsu.validation.executors.impl.MaxLengthRuleExecutor;
import org.springjutsu.validation.rules.ValidationRulesContainer;
import org.springjutsu.validation.test.entities.Customer;

@RunWith(MockitoJUnitRunner.class)
public class ValidationManagerTest {

	@Mock
	private ValidationRulesContainer container;
	
	@InjectMocks
	private ValidationManager validationManager = new ValidationManager();
	
	@Before
	public void setup() {
		validationManager.rulesContainer = container;
	}
	
	@Test
	public void testSupportsClassOfQ() {
		Mockito.when(container.supportsClass(String.class)).thenReturn(true);
		Mockito.when(container.supportsClass(List.class)).thenReturn(false);

		assertTrue(validationManager.supports(String.class));
		assertFalse(validationManager.supports(List.class));
	}
	
	@Test
	public void testConvertNullRuleArgument() {
		Object converted = validationManager.convertRuleArgument(null, new MaxLengthRuleExecutor());
		assertNull(converted);
	}
	
	@Test
	public void testUnneccessaryConvertRuleArgument() {
		Object converted = validationManager.convertRuleArgument(5, new MaxLengthRuleExecutor());
		assertEquals(5, converted);
		
		converted = validationManager.convertRuleArgument("5", new MatchesRuleExecutor());
		assertEquals("5", converted);
	}
	
	@Test
	public void testConvertRuleArgument() {
		Object converted = validationManager.convertRuleArgument("5", new MaxLengthRuleExecutor());
		assertEquals(5, converted);
	}
	
	@Test(expected=TypeMismatchException.class)
	public void testUnconvertibleRuleARgument() {
		validationManager.convertRuleArgument(new Customer(), new MaxLengthRuleExecutor());
	}
}
