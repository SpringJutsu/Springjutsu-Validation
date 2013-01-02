package org.springjutsu.validation.rules;

public enum CollectionStrategy {
	
	VALIDATE_MEMBERS("validateMembers"),
	VALIDATE_COLLECTION_OBJECT("validateCollectionObject");
	
	private String xmlValue;
	
	CollectionStrategy(String xmlValue) {
		this.xmlValue = xmlValue;
	}
	
	public static CollectionStrategy forXmlValue(String xmlValue) {
		CollectionStrategy match = null;
		for (CollectionStrategy strategy : values()) {
			if (strategy.getXmlValue().equals(xmlValue)) {
				match = strategy;
			}
		}
		return match;
	}
	
	public String getXmlValue() {
		return this.xmlValue;
	}

}
