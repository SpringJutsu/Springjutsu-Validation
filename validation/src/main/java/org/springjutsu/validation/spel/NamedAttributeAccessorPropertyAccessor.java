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

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;

public class NamedAttributeAccessorPropertyAccessor implements PropertyAccessor {

	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getSpecificTargetClasses() {
		return new Class[] {AbstractNamedAttributeAccessor.class};
	}

	@Override
	public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
		return target != null && ((AbstractNamedAttributeAccessor) target).get(name) != null;
	}

	@Override
	public TypedValue read(EvaluationContext context, Object target, String name)
			throws AccessException {
		return new TypedValue(((AbstractNamedAttributeAccessor) target).get(name));
	}

	@Override
	public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
		return ((AbstractNamedAttributeAccessor) target).isWritable();
	}

	@Override
	public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
		((AbstractNamedAttributeAccessor) target).set(name, newValue);
	}

}
