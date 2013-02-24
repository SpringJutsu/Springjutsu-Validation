package org.springjutsu.validation.namespace;

/**
 * Used by @see{ValidationConfigurationParser} to register beans wired in the XML config.
 * Or at least until I figure out the "correct" way...
 * @author Clark Duplichien
 */
public class KeyedBeanRegistrant {
	
	private String key;
	private String beanName;
	
	public KeyedBeanRegistrant(String beanName, String key) {
		this.key = key;
		this.beanName = beanName;
	}

	public String getKey() {
		return key;
	}

	public String getBeanName() {
		return beanName;
	}
}
