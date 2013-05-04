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

package org.springjutsu.validation.spel;

import org.springframework.binding.collection.MapAdaptable;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.webflow.expression.spel.MapAdaptablePropertyAccessor;

/**
 * Workaround for SWF-1472: super.canRead always returns true.
 * @author Clark Duplichien
 */
public class ReadCheckingMapAdaptablePropertyAccessor 
	extends MapAdaptablePropertyAccessor {
	
	/**
	 * Do a check instead of always returning true.
	 */
	@Override
	public boolean canRead(EvaluationContext context, Object target, String name)
			throws AccessException {
		MapAdaptable map = (MapAdaptable) target;
		return map.asMap().get(name) != null;
	}

}
