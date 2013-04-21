/*
 * Copyright 2010-2013 Duplichien, Wicksell, Springjutsu.org
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
