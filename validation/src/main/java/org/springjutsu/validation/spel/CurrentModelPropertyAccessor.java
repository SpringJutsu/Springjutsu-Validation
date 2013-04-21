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
import org.springframework.expression.spel.support.ReflectivePropertyAccessor;
import org.springjutsu.validation.ValidationEvaluationContext.CurrentModelAccessor;

/**
 * Makes the object currently under validation,
 * i.e. the object represented by the combination of
 * nested and template paths, referenceable from SPEL 
 * expressions in the validation XML files.
 * Delegate directly to the current model accessed by
 * a @see CurrentModelAccessor using a standard @see ReflectivePropertyAccessor
 * @author Clark Duplichien
 */
public class CurrentModelPropertyAccessor implements PropertyAccessor {
	
	private ReflectivePropertyAccessor subPropertyAccessor = new ReflectivePropertyAccessor();

	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getSpecificTargetClasses() {
		return new Class[]{CurrentModelAccessor.class};
	}

	@Override
	public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
		return subPropertyAccessor.canRead(context, ((CurrentModelAccessor) target).accessCurrentModel(), name);
	}

	@Override
	public TypedValue read(EvaluationContext context, Object target, String name)
			throws AccessException {
		return subPropertyAccessor.read(context, ((CurrentModelAccessor) target).accessCurrentModel(), name);
	}

	@Override
	public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
		return subPropertyAccessor.canWrite(context, ((CurrentModelAccessor) target).accessCurrentModel(), name);
	}

	@Override
	public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
		subPropertyAccessor.write(context, ((CurrentModelAccessor) target).accessCurrentModel(), name, newValue);		
	}

}
