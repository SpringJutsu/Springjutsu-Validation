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
 * Allows rules whose model is a collection object 
 * to indicate the preferred strategy for applying 
 * the rule, either to individual collection members,
 * or the collection object as a whole 
 * @author Clark Duplichien
 *
 */
public enum CollectionStrategy {
	
	/**
	 * Treat each individual member of the collection 
	 * as the model for the rule, applying any errors 
	 * to the collection index for the member(s) on 
	 * which the rule failed.
	 */
	VALIDATE_MEMBERS("validateMembers"),
	
	/**
	 * Pass the collection object itself as the model
	 * to the rule.
	 */
	VALIDATE_COLLECTION_OBJECT("validateCollectionObject");
	
	private String xmlValue;
	
	/**
	 * Default constructor
	 * @param xmlValue the value as it appears in the XML 
	 * rule configuration files
	 */
	CollectionStrategy(String xmlValue) {
		this.xmlValue = xmlValue;
	}
	
	/**
	 * @param xmlValue The value passed in an XML rule configuration file
	 * @return The matching CollectionStrategy enumerated value
	 */
	public static CollectionStrategy forXmlValue(String xmlValue) {
		CollectionStrategy match = null;
		for (CollectionStrategy strategy : values()) {
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
