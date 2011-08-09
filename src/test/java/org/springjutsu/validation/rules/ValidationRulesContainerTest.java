
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
		HashMap entities = new HashMap();
		entities.put("mockValidationEntity", validationEntity);
		ArrayList rules = new ArrayList();
		rules.add(validationRule);
		
		Mockito.when(validationEntity.getValidationClass()).thenReturn(List.class);
		Mockito.when(validationEntity.getContextValidationRules("supportedState")).thenReturn(rules);
		Mockito.when(validationEntity.getModelValidationRules()).thenReturn(rules);

		Mockito.when(beanFactory.getBeansOfType(ValidationEntity.class)).thenReturn(entities);
		container.setBeanFactory(beanFactory);
	}
	
	@Test
	public void testGetValidationEntity() {
		assertEquals(validationEntity, container.getValidationEntity(List.class));
	}

	@Test
	public void testGetContextRules() {
		assertNotNull(container.getContextRules(List.class, "supportedState"));
		assertFalse(container.getContextRules(List.class, "supportedState").isEmpty());
		assertEquals(validationRule, container.getContextRules(List.class, "supportedState").get(0));
		assertNotNull(container.getContextRules(List.class, "unSupportedState"));
		assertTrue(container.getContextRules(List.class, "unSupportedState").isEmpty());
		assertNotNull(container.getContextRules(String.class, "supportedState"));
		assertTrue(container.getContextRules(String.class, "supportedState").isEmpty());
		assertNotNull(container.getContextRules(String.class, "unSupportedState"));
		assertTrue(container.getContextRules(String.class, "unSupportedState").isEmpty());
	}

	@Test
	public void testGetModelRules() {
		assertNotNull(container.getModelRules(List.class));
		assertFalse(container.getModelRules(List.class).isEmpty());
		assertEquals(validationRule, container.getModelRules(List.class).get(0));
		assertNotNull(container.getModelRules(String.class));
		assertTrue(container.getModelRules(String.class).isEmpty());
	}

	@Test
	public void testHasModelRulesForClass() {
		assertTrue(container.hasModelRulesForClass(List.class));
		assertFalse(container.hasModelRulesForClass(String.class));
	}

	@Test
	public void testSupportsClass() {
		assertTrue(container.supportsClass(List.class));
		assertFalse(container.supportsClass(String.class));
	}

}
