
package org.springjutsu.validation.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
	public void setup()
	{
		HashMap<String, ValidationEntity> entities = new HashMap<String, ValidationEntity>();
		entities.put("mockValidationEntity", validationEntity);
		ArrayList<ValidationRule> rules = new ArrayList<ValidationRule>();
		rules.add(validationRule);
		
		Mockito.when(validationEntity.getValidationClass()).thenReturn(List.class);
		Mockito.when(validationEntity.getValidationRules("supportedState")).thenReturn(rules);
		Mockito.when(validationEntity.getValidationRules(null)).thenReturn(rules);
		Mockito.when(validationEntity.getRules()).thenReturn(rules);

		Mockito.when(beanFactory.getBeansOfType(ValidationEntity.class)).thenReturn(entities);
		container.setBeanFactory(beanFactory);
	}
	
	@Test
	public void testGetValidationEntity() {
		assertEquals(validationEntity, container.getValidationEntity(List.class));
	}

	@Test
	public void testGetFormSpecificRules() {
		assertNotNull(container.getRules(List.class, "supportedState"));
		assertFalse(container.getRules(List.class, "supportedState").isEmpty());
		assertEquals(validationRule, container.getRules(List.class, "supportedState").get(0));
		assertNotNull(container.getRules(List.class, "unSupportedState"));
		assertTrue(container.getRules(List.class, "unSupportedState").isEmpty());
		assertNotNull(container.getRules(String.class, "supportedState"));
		assertTrue(container.getRules(String.class, "supportedState").isEmpty());
		assertNotNull(container.getRules(String.class, "unSupportedState"));
		assertTrue(container.getRules(String.class, "unSupportedState").isEmpty());
	}

	@Test
	public void testGetBaseRules() {
		assertNotNull(container.getRules(List.class, null));
		assertFalse(container.getRules(List.class, null).isEmpty());
		assertEquals(validationRule, container.getRules(List.class, null).get(0));
		assertNotNull(container.getRules(String.class, null));
		assertTrue(container.getRules(String.class, null).isEmpty());
	}

	@Test
	public void testHasModelRulesForClass() {
		assertTrue(container.hasRulesForClass(List.class));
		assertFalse(container.hasRulesForClass(String.class));
	}

	@Test
	public void testSupportsClass() {
		assertTrue(container.supportsClass(List.class));
		assertFalse(container.supportsClass(String.class));
	}

}
