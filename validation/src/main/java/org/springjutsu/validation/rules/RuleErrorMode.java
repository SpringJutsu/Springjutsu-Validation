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

package org.springjutsu.validation.rules;

/**
 * Allows rules to specify an explicit strategy for dealing
 * with rule failures. E.g. allows a rule with children to still
 * produce an error message on rule failure.
 * @author Clark Duplichien
 *
 */
public enum RuleErrorMode {
	
	/**
	 * Acts as ERROR when no child rules are present,
	 * or as SKIP_CHILDREN when child rules are present.
	 */
	DEFAULT("default"),
	
	/**
	 * Will force the rule to produce an error on failure,
	 * even if the rule has children.
	 */
	ERROR("error"),
	
	/**
	 * Will not produce an error; will instead only skip
	 * any child rules specified within this rule.
	 */
	SKIP_CHILDREN("skipChildren");
	
	private String xmlValue;
	
	/**
	 * Default constructor
	 * @param xmlValue the value as it appears in the XML 
	 * rule configuration files
	 */
	RuleErrorMode(String xmlValue) {
		this.xmlValue = xmlValue;
	}
	
	/**
	 * @param xmlValue The value passed in an XML rule configuration file
	 * @return The matching CollectionStrategy enumerated value
	 */
	public static RuleErrorMode forXmlValue(String xmlValue) {
		RuleErrorMode match = null;
		for (RuleErrorMode strategy : values()) {
			if (strategy.getXmlValue().equals(xmlValue)) {
				match = strategy;
			}
		}
		return match;
	}
	
	/**
	 * @return the XML value for the current collection strategy
	 */
	public String getXmlValue() {
		return this.xmlValue;
	}

}
