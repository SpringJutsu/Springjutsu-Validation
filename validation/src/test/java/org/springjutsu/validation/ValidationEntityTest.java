package org.springjutsu.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springjutsu.validation.rules.ValidationEntity;
import org.springjutsu.validation.rules.ValidationRule;
import org.springjutsu.validation.rules.ValidationTemplate;
import org.springjutsu.validation.rules.ValidationTemplateReference;

@RunWith(MockitoJUnitRunner.class)
public class ValidationEntityTest {
	
	@Mock
	private ArrayList<ValidationRule> rules;
	
	@Mock
	private ArrayList<ValidationTemplateReference> templateReferences;
	
	@Mock
	private ArrayList<ValidationTemplate> validationTemplates;
	
	@Mock
	private HashMap<String, List<ValidationRule>> formRuleCache;
	
	@InjectMocks
	ValidationEntity entity = new ValidationEntity();
	
	@Mock
	private ValidationRule rule;
	
	@Mock
	private ValidationTemplateReference templateReference;
	
	@Mock
	private ValidationTemplate template;
	
	@Test
	public void testAddRule() {
		entity.addRule(rule);
		Mockito.verify(rules, Mockito.atLeastOnce()).add(rule);
	}
	
	@Test
	public void testAddTemplateReference() {
		entity.addTemplateReference(templateReference);
		Mockito.verify(templateReferences, Mockito.atLeastOnce()).add(templateReference);
	}
	
	@Test
	public void testAddValidationTemplate() {
		entity.addValidationTemplate(template);
		Mockito.verify(validationTemplates, Mockito.atLeastOnce()).add(template);
	}
	
	@Test
	public void testGetCachedRulesForForm() {
		String formName = "foo";
		Mockito.when(formRuleCache.containsKey(formName)).thenReturn(true);
		entity.getValidationRules(formName);
		Mockito.verify(formRuleCache, Mockito.atLeastOnce()).get(formName);
	}
	
	@Test
	public void testGetValidationRulesChecksAppliesToForm() {
		String formName = "foo";
		List<ValidationRule> mockRules = new ArrayList<ValidationRule>();
		ValidationRule applicableRule = Mockito.mock(ValidationRule.class);
		Mockito.when(applicableRule.appliesToForm(formName)).thenReturn(true);
		mockRules.add(applicableRule);
		ValidationRule nonApplicableRule = Mockito.mock(ValidationRule.class);
		Mockito.when(nonApplicableRule.appliesToForm(formName)).thenReturn(false);
		mockRules.add(nonApplicableRule);
		Mockito.when(rules.iterator()).thenReturn(mockRules.iterator());
		List<ValidationRule> applicableRules = entity.getValidationRules(formName);
		assertTrue(applicableRules.contains(applicableRule));
		assertFalse(applicableRules.contains(nonApplicableRule));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testDiscoveredRulesForFormAreCached() {
		String formName = "foo";
		List<ValidationRule> mockRules = new ArrayList<ValidationRule>();
		Mockito.when(rules.iterator()).thenReturn(mockRules.iterator());
		entity.getValidationRules(formName);
		Mockito.verify(formRuleCache, Mockito.atLeastOnce()).put(Mockito.anyString(), Mockito.anyList());
	}

}
