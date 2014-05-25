
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ValidationRulesContainerTest {
	
	@Mock
	ValidationEntity validationEntity;
	
	@Mock
	ValidationRule validationRule;
	
	@InjectMocks
	ValidationRulesContainer container = new ValidationRulesContainer();
	
	@Before
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setup() {
		List<ValidationEntity> entityList = new ArrayList<ValidationEntity>();
		
		entityList.add(validationEntity);
		ArrayList<ValidationRule> rules = new ArrayList<ValidationRule>();
		rules.add(validationRule);
		
		Mockito.when(validationEntity.getValidationClass()).thenReturn((Class) List.class);
		Mockito.when(validationEntity.getRules()).thenReturn(rules);
		
		container.setValidationEntities(entityList);
		container.initializeValdationEntities();
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
