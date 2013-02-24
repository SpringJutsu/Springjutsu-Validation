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
