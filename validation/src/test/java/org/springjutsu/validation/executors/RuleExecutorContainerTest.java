package org.springjutsu.validation.executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.ListableBeanFactory;

@RunWith(MockitoJUnitRunner.class)
public class RuleExecutorContainerTest {
	
	
	RuleExecutorContainer executorContainer = new RuleExecutorContainer();
	
	@Mock
	ListableBeanFactory beanFactory;
	
	RuleExecutor<?, ?> executor;
	
	RuleExecutor<?, ?> proxiedRuleExecutor;
	
	@Before
	public void setup()
	{
		executor = new AnnotatedRuleExecutor();
		proxiedRuleExecutor = new ProxiedAnnotatedRuleExecutor();
		HashMap<String, Object> executors = new HashMap<String, Object>();
		executors.put("testExecutor", executor);
		executors.put("proxiedExecutor", proxiedRuleExecutor);
		Mockito.when(beanFactory.getBeansWithAnnotation(ConfiguredRuleExecutor.class)).thenReturn(executors);
		executorContainer.beanFactory = beanFactory;
	}

	@Test
	public void testDiscoverAnnotatedRuleExecutors() {
		executorContainer.registerRuleExecutors();
		assertEquals(executor, executorContainer.getRuleExecutorByName("testExecutor"));
		assertEquals(proxiedRuleExecutor, executorContainer.getRuleExecutorByName("proxiedExecutor"));
	}


	@Test
	public void testSetCustomRuleExecutorStringRuleExecutor() {
		executor = new AnnotatedRuleExecutor();
		HashMap<String, RuleExecutor<?,?>> executors = new HashMap<String, RuleExecutor<?,?>>();
		executors.put("testExecutor", executor);
		executorContainer.setCustomRuleExecutors(executors);
		assertEquals(executor, executorContainer.getRuleExecutorByName("testExecutor"));
		try
		{
			executorContainer.setCustomRuleExecutor("testExecutor", new AnnotatedRuleExecutor());
			fail();
		}
		catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().startsWith("Implementation for rule name \"testExecutor\" already set to type "));
		}
	}

	@Test
	public void testAddDefaultRuleExecutors() {
		executorContainer.registerRuleExecutors();
		assertNotNull(executorContainer.getRuleExecutorByName("required"));
	}

	@Test
	public void testSetAddDefaultRuleExecutors() {
		executorContainer.setAddDefaultRuleExecutors(false);
		executorContainer.registerRuleExecutors();
		try 
		{
			executorContainer.getRuleExecutorByName("required");
			fail();
		}
		catch (IllegalArgumentException e) {
			assertEquals("No rule executor with name: required", e.getMessage());
		}
	}

	@ConfiguredRuleExecutor(name = "testExecutor")
	private class AnnotatedRuleExecutor implements RuleExecutor<Object, Object>
	{
		
		@Override
		public boolean validate(Object model, Object argument) throws Exception {
			return false;
		}
	}
	
	@ConfiguredRuleExecutor(name = "proxiedExecutor")
	private class ToBeProxiedAnnotatedRuleExecutor implements RuleExecutor<Object, Object>
	{
		
		@Override
		public boolean validate(Object model, Object argument) throws Exception {
			return false;
		}
	}
	
	private class ProxiedAnnotatedRuleExecutor extends ToBeProxiedAnnotatedRuleExecutor {
		
	}
}
