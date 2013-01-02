package org.springjutsu.validation.test.entities;

import java.util.ArrayList;
import java.util.List;

import org.springjutsu.validation.rules.SkipValidation;

public class SkippablePerson {
	
	private String name;

	@SkipValidation
	private SkippablePerson skipMe;
	
	@CustomSkipValidation
	private SkippablePerson customSkipMe;
	
	private SkippablePerson skipMeFromXml;
	
	private SkippablePerson dontSkipMeBro;
	
	@SkipValidation
	private List<SkippablePerson> skipUs = new ArrayList<SkippablePerson>();
	
	private List<SkippablePerson> skipUsFromXml = new ArrayList<SkippablePerson>();
	
	private List<SkippablePerson> dontSkipUsBro = new ArrayList<SkippablePerson>();

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

	public SkippablePerson getDontSkipMeBro() {
		return dontSkipMeBro;
	}

	public void setDontSkipMeBro(SkippablePerson dontSkipMeBro) {
		this.dontSkipMeBro = dontSkipMeBro;
	}

	public List<SkippablePerson> getSkipUs() {
		return skipUs;
	}

	public void setSkipUs(List<SkippablePerson> skipUs) {
		this.skipUs = skipUs;
	}

	public List<SkippablePerson> getSkipUsFromXml() {
		return skipUsFromXml;
	}

	public void setSkipUsFromXml(List<SkippablePerson> skipUsFromXml) {
		this.skipUsFromXml = skipUsFromXml;
	}

	public List<SkippablePerson> getDontSkipUsBro() {
		return dontSkipUsBro;
	}

	public void setDontSkipUsBro(List<SkippablePerson> dontSkipUsBro) {
		this.dontSkipUsBro = dontSkipUsBro;
	}
}
