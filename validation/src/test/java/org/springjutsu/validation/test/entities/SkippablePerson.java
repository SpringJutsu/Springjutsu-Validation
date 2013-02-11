package org.springjutsu.validation.test.entities;

import java.util.ArrayList;
import java.util.List;

import org.springjutsu.validation.rules.RecursiveValidationExclude;

public class SkippablePerson {
	
	private String name;

	@RecursiveValidationExclude
	private SkippablePerson skipMe;
	
	@CustomSkipValidation
	private SkippablePerson customSkipMe;
	
	private SkippablePerson skipMeFromXml;
	
	private SkippablePerson dontSkipMeFromXml;
	
	@RecursiveValidationExclude
	private List<SkippablePerson> skipUs = new ArrayList<SkippablePerson>();
	
	@CustomSkipValidation
	private List<SkippablePerson> customSkipUs = new ArrayList<SkippablePerson>();
	
	private List<SkippablePerson> skipUsFromXml = new ArrayList<SkippablePerson>();
	
	private List<SkippablePerson> dontSkipUsFromXml = new ArrayList<SkippablePerson>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SkippablePerson getSkipMe() {
		return skipMe;
	}

	public void setSkipMe(SkippablePerson skipMe) {
		this.skipMe = skipMe;
	}

	public SkippablePerson getCustomSkipMe() {
		return customSkipMe;
	}

	public void setCustomSkipMe(SkippablePerson customSkipMe) {
		this.customSkipMe = customSkipMe;
	}

	public SkippablePerson getSkipMeFromXml() {
		return skipMeFromXml;
	}

	public void setSkipMeFromXml(SkippablePerson skipMeFromXml) {
		this.skipMeFromXml = skipMeFromXml;
	}

	public SkippablePerson getDontSkipMeFromXml() {
		return dontSkipMeFromXml;
	}

	public void setDontSkipMeFromXml(SkippablePerson dontSkipMeFromXml) {
		this.dontSkipMeFromXml = dontSkipMeFromXml;
	}

	public List<SkippablePerson> getSkipUs() {
		return skipUs;
	}

	public void setSkipUs(List<SkippablePerson> skipUs) {
		this.skipUs = skipUs;
	}
	
	public List<SkippablePerson> getCustomSkipUs() {
		return customSkipUs;
	}

	public void setCustomSkipUs(List<SkippablePerson> customSkipUs) {
		this.customSkipUs = customSkipUs;
	}

	public List<SkippablePerson> getSkipUsFromXml() {
		return skipUsFromXml;
	}

	public void setSkipUsFromXml(List<SkippablePerson> skipUsFromXml) {
		this.skipUsFromXml = skipUsFromXml;
	}

	public List<SkippablePerson> getDontSkipUsFromXml() {
		return dontSkipUsFromXml;
	}

	public void setDontSkipUsFromXml(List<SkippablePerson> dontSkipUsFromXml) {
		this.dontSkipUsFromXml = dontSkipUsFromXml;
	}
	
}
