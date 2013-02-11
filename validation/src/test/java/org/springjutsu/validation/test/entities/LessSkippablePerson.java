package org.springjutsu.validation.test.entities;

import java.util.ArrayList;
import java.util.List;

import org.springjutsu.validation.rules.RecursiveValidationInclude;

public class LessSkippablePerson extends SkippablePerson {
	
	@RecursiveValidationInclude
	private LessSkippablePerson dontSkipMe;
	
	@CustomValidateRecursively
	private LessSkippablePerson customDontSkipMe;

	@RecursiveValidationInclude
	private List<LessSkippablePerson> dontSkipUs = new ArrayList<LessSkippablePerson>();
	
	@CustomValidateRecursively
	private List<LessSkippablePerson> customDontSkipUs = new ArrayList<LessSkippablePerson>();
	
	public LessSkippablePerson getDontSkipMe() {
		return dontSkipMe;
	}

	public void setDontSkipMe(LessSkippablePerson dontSkipMe) {
		this.dontSkipMe = dontSkipMe;
	}
	
	public LessSkippablePerson getCustomDontSkipMe() {
		return customDontSkipMe;
	}

	public void setCustomDontSkipMe(LessSkippablePerson customDontSkipMe) {
		this.customDontSkipMe = customDontSkipMe;
	}
	
	public List<LessSkippablePerson> getDontSkipUs() {
		return dontSkipUs;
	}

	public void setDontSkipUs(List<LessSkippablePerson> dontSkipUs) {
		this.dontSkipUs = dontSkipUs;
	}
	
	public List<LessSkippablePerson> getCustomDontSkipUs() {
		return customDontSkipUs;
	}

	public void setCustomDontSkipUs(List<LessSkippablePerson> customDontSkipUs) {
		this.customDontSkipUs = customDontSkipUs;
	}
}
