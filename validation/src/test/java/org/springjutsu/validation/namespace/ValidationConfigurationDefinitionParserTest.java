package org.springjutsu.validation.namespace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springjutsu.validation.ValidationErrorMessageHandler;
import org.springjutsu.validation.ValidationManager;
import org.springjutsu.validation.executors.RuleExecutorContainer;
import org.springjutsu.validation.executors.ValidWhenEmptyRuleExecutor;
import org.springjutsu.validation.rules.ValidationRulesContainer;

@ContextConfiguration(
	"classpath:/org/springjutsu/validation/namespace/validationConfigurationDefinitionParserTest-config.xml")
@RunWith(SpringRunner.class)
public class ValidationConfigurationDefinitionParserTest {

	@Autowired(required=false)
	private ValidationManager validationManager;

	@Autowired
	private BeanFactory beanFactory;

	@Autowired(required=false)
	private RuleExecutorContainer executorContainer;

	@Autowired(required=false)
	private ValidationRulesContainer rulesContainer;

	@Autowired(required=false)
	private ValidationErrorMessageHandler errorMessageHandler;

	/**
	 * Ensure validation manager is registered.
	 */
	@Test
	public void testValidationManagerPresent() {
		assertNotNull(validationManager);
	}

	/**
	 * Ensure validation manager is registered
	 * with the given bean name.
	 */
	@Test
	public void testValidationManagerNamed() {
		assertTrue(beanFactory.containsBean("testValidationManagerName"));
		assertEquals(beanFactory.getBean("testValidationManagerName"), validationManager);
	}

	/**
	 * Ensure message-config prefixes are set accordingly.
	 */
	@Test
	public void testMessageConfiguration() {
		assertEquals("testErrorsPrefix.", errorMessageHandler.getErrorMessagePrefix());
		assertEquals("testFieldLabelPrefix.", errorMessageHandler.getFieldLabelPrefix());
	}

	/**
	 * Ensure a rules container has been registered.
	 */
	@Test
	public void testValidationRuleContainerPresent() {
		assertNotNull(rulesContainer);
	}

	/**
	 * Ensure a rule executor container has been registered.
	 */
	@Test
	public void testRuleExecutorContainerPresent() {
		assertNotNull(executorContainer);
	}

	/**
	 * Ensure default rule executors can be suppressed.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testRuleExecutorDefaultsSuppressed() {
		executorContainer.getRuleExecutorByName("alphabetic");
	}

	/**
	 * Ensure XML configuration of rule executors is functioning.
	 */
	@Test
	public void testRuleExecutorRegistered() {
		assertNotNull(executorContainer.getRuleExecutorByName("test"));
	}

	/**
	 * Ensure bean properties of registered rule executors
	 * have been wired properly.
	 */
	@Test
	public void testRuleExecutorRegisteredAsBean() {
		assertEquals(beanFactory,
			((TestBeanPropertyRuleExecutor)executorContainer.getRuleExecutorByName("test")).beanFactory);
	}

	/**
	 * Used to test that bean properties are wired on context registered executors.
	 */
	public static class TestBeanPropertyRuleExecutor extends ValidWhenEmptyRuleExecutor<Object, Object> {
		@Autowired(required=false) public BeanFactory beanFactory;
		@Override public boolean doValidate(Object model, Object argument) { return true; }
	}
}
