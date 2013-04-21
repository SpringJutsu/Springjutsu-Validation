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

package org.springjutsu.validation.context;

import java.util.Arrays;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springjutsu.validation.spel.SPELResolver;

/**
 * Effectively matches JSR-303 validation groups
 * functionality by activating whenever one of
 * the current validation hints is an exact
 * string match to a specified context qualifier.
 * Used in conjunction with the &lt;group&gt; element
 * in XML validation.
 * @author Clark Duplichien
 */
public class ValidationGroupContextHandler implements ValidationContextHandler {

	/**
	 * Validation groups are active if any 
	 * of the specified qualifiers is an exact 
	 * match to any of the currently specified
	 * validation hints (group names)
	 */
	@Override
	public boolean isActive(Set<String> qualifiers, Object rootModel, String[] validationHints) {
		return !CollectionUtils.intersection(qualifiers, Arrays.asList(validationHints)).isEmpty();
	}

	/**
	 * Don't see why not.
	 */
	@Override
	public boolean enableDuringSubBeanValidation() {
		return true;
	}

	/**
	 * Validation groups have nothing
	 * useful to supply to a SPEL context.
	 */
	@Override
	public void initializeSPELResolver(SPELResolver spelResolver) {
		return;
	}

}
