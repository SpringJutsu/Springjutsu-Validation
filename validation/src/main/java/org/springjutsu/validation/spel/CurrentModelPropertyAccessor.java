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
