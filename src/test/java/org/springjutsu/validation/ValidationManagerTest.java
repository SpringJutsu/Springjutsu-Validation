package org.springjutsu.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.Errors;
import org.springjutsu.validation.rules.ValidationRulesContainer;

@RunWith(MockitoJUnitRunner.class)
public class ValidationManagerTest {

	@Mock
	ValidationRulesContainer container;
	
	@Mock(answer=Answers.CALLS_REAL_METHODS)
	ValidationManager validationManager;
	
	@Mock
	Errors errors;
	
	@Before
	public void setup()
	{
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
	public void testValidate() {
//		validationManager.validate(new TestModel("test"), errors);
	}

	
	private class TestModel
	{
		private String testString;
		
		public TestModel(String testString) {
			this.testString = testString;
		}
	}
}
