/*
 * Copyright 2010-2011 Duplichien, Wicksell, Springjutsu.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springjutsu.validation.executors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springjutsu.validation.executors.impl.AlphabeticRuleExecutor;
import org.springjutsu.validation.executors.impl.AlphanumericRuleExecutor;
import org.springjutsu.validation.executors.impl.EmailRuleExecutor;
import org.springjutsu.validation.executors.impl.ExactLengthRuleExecutor;
import org.springjutsu.validation.executors.impl.MatchesRuleExecutor;
import org.springjutsu.validation.executors.impl.MaxLengthRuleExecutor;
import org.springjutsu.validation.executors.impl.MinLengthRuleExecutor;
import org.springjutsu.validation.executors.impl.NumericRuleExecutor;
import org.springjutsu.validation.executors.impl.RequiredRuleExecutor;
import org.springjutsu.validation.namespace.KeyedBeanRegistrant;

/**
 * Stores all discovered named rule executors, and makes them available
 * as validation rule types. Will discover both rule executors which are 
 * provided through the customRuleExecutors property, as well as those which are
 * annotated with @ConfiguredRuleExecutor 
 * @author Clark Duplichien
 *
 */
public class RuleExecutorContainer {
	
	/**
	 * A map of rule name to the executor.
	 */
	protected Map<String, RuleExecutor> ruleExecutors = new HashMap<String, RuleExecutor>();
	
	/**
	 * A list of rule executors registered as beans
	 */
	protected List<KeyedBeanRegistrant> beanRegistrants;
	
	/**
	 * Can configure this to false if user doesn't want the default executors. 
	 */
	protected boolean addDefaultRuleExecutors = true;
	
	/**
	 * Use the bean factory to look up annotated rule executors.
	 */
	@Autowired
	protected BeanFactory beanFactory;
	
	/**
	 * Finds the annotated rule executors by searching the bean factory.
	 * Also registers XML-configured rule executors.
	 * @throws BeansException on a bad.
	 */
	@PostConstruct
	public void registerRuleExecutors() throws BeansException {
		if (addDefaultRuleExecutors) {
			addDefaultRuleExecutors();
		}
		Map<String, Object> ruleExecutorBeans = 
			((ListableBeanFactory) beanFactory).getBeansWithAnnotation(ConfiguredRuleExecutor.class);

		for (String springName : ruleExecutorBeans.keySet()) {
			RuleExecutor ruleExecutor = (RuleExecutor) ruleExecutorBeans.get(springName);
			String ruleName = ruleExecutor.getClass().getAnnotation(ConfiguredRuleExecutor.class).name();
			setCustomRuleExecutor(ruleName, ruleExecutor);
		}
		if (beanRegistrants != null) {
			for (KeyedBeanRegistrant registrant : beanRegistrants) {
				setCustomRuleExecutor(registrant.getKey(), (RuleExecutor) beanFactory.getBean(registrant.getBeanName()));
			}
		}
	}

	/**
	 * Set custom rule executors with specific names.
	 * @param customRuleExecutors the rules to set.
	 */
	public void setCustomRuleExecutors(Map<String, RuleExecutor> customRuleExecutors) {
		for (String executorName : customRuleExecutors.keySet()) {
			setCustomRuleExecutor(executorName, customRuleExecutors.get(executorName));
		}
	}
	
	/**
	 * Set custom rule executor with a specific name.
	 * Throws IllegalArgumentException if a rule already exists with that name.
	 * @param executorName Name of the rule to set
	 * @param ruleExecutor The rule executor to set.
	 */
	public void setCustomRuleExecutor(String executorName, RuleExecutor ruleExecutor) {
		if (ruleExecutors.containsKey(executorName)) {
			throw new IllegalArgumentException(
				"Implementation for rule name \"" + executorName 
				+ "\" already set to type " 
				+ ruleExecutors.get(executorName).getClass().getCanonicalName());
		} else {
			ruleExecutors.put(executorName, ruleExecutor);
		}
	}
	
	/**
	 * Gets a rule executor by rule name.
	 * @param executorName Name of the rule executor.
	 * @return RuleExecutor with that name.
	 */
	public RuleExecutor getRuleExecutorByName(String executorName) {
		if (ruleExecutors.containsKey(executorName)) {
			return ruleExecutors.get(executorName);
		}
		throw new IllegalArgumentException("No rule executor with name: " + executorName);
	}
	
	/**
	 * Instantiates and adds some basic default rules.
	 */
	protected void addDefaultRuleExecutors()
	{
		setCustomRuleExecutor("alphabetic", new AlphabeticRuleExecutor());
		setCustomRuleExecutor("alphanumeric", new AlphanumericRuleExecutor());
		setCustomRuleExecutor("email", new EmailRuleExecutor());
		setCustomRuleExecutor("maxLength", new MaxLengthRuleExecutor());
		setCustomRuleExecutor("minLength", new MinLengthRuleExecutor());
		setCustomRuleExecutor("exactLength", new ExactLengthRuleExecutor());
		setCustomRuleExecutor("numeric", new NumericRuleExecutor());
		setCustomRuleExecutor("required", new RequiredRuleExecutor());
		setCustomRuleExecutor("notEmpty", new RequiredRuleExecutor());
		setCustomRuleExecutor("matches", new MatchesRuleExecutor());
	}

	/**
	 * Set to false if user does not want the default rule executors.
	 * @param addDefaultRuleExecutors
	 */
	public void setAddDefaultRuleExecutors(boolean addDefaultRuleExecutors) {
		this.addDefaultRuleExecutors = addDefaultRuleExecutors;
	}
	
	/**
	 * Hook by which @see{ValidationConfigurationParser} registers XML defined rule executors
	 * @param registrants @see{ValidationConfigurationParser} RuleExecutorBeanRegistrants to register.
	 */
	public void setRuleExecutorBeanRegistrants(List<KeyedBeanRegistrant> registrants) {
		this.beanRegistrants = registrants;
	}
}
