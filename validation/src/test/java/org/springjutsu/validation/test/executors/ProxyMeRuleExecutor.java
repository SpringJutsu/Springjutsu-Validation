package org.springjutsu.validation.test.executors;

import org.springjutsu.validation.executors.ConfiguredRuleExecutor;
import org.springjutsu.validation.executors.impl.MaxLengthRuleExecutor;

@ConfiguredRuleExecutor(name="proxyMe")
public class ProxyMeRuleExecutor extends MaxLengthRuleExecutor {

}
