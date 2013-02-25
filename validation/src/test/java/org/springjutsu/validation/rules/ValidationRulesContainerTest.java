
package org.springjutsu.validation.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.ListableBeanFactory;

@RunWith(MockitoJUnitRunner.class)
public class ValidationRulesContainerTest {

	@Mock(answer=Answers.CALLS_REAL_METHODS)
	ValidationRulesContainer container;
	
	@Mock
	ListableBeanFactory beanFactory;
	
	@Mock
	ValidationEntity validationEntity;
	
	@Mock
	ValidationRule validationRule;
	
	@Before
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setup()
	{
		HashMap<String, ValidationEntity> entities = new HashMap<String, ValidationEntity>();
		entities.put("mockValidationEntity", validationEntity);
		ArrayList<ValidationRule> rules = new ArrayList<ValidationRule>();
		rules.add(validationRule);
		
		Mockito.when(validationEntity.getValidationClass()).thenReturn((Class) List.class);
		Mockito.when(validationEntity.getRules()).thenReturn(rules);

		Mockito.when(beanFactory.getBeansOfType(ValidationEntity.class)).thenReturn(entities);
		container.setBeanFactory(beanFactory);
	}
	
	@Test
	public void testGetValidationEntity() {
		assertEquals(validationEntity, container.getValidationEntity(List.class));
	}

	@Test
	public void testHasRulesForClass() {
		assertTrue(container.hasRulesForClass(List.class));
		assertFalse(container.hasRulesForClass(String.class));
	}

	@Test
	public void testSupportsClass() {
		assertTrue(container.supportsClass(List.class));
		assertFalse(container.supportsClass(String.class));
	}

}
