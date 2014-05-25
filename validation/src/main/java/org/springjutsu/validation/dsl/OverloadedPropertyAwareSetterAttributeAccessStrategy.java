package org.springjutsu.validation.dsl;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtils;

import com.fluentinterface.proxy.impl.SetterAttributeAccessStrategy;

public class OverloadedPropertyAwareSetterAttributeAccessStrategy extends SetterAttributeAccessStrategy {

	@Override
	public void setPropertyValue(Object target, String property, Object value) throws IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {
		MethodUtils.invokeMethod(target,  PropertyUtils.getPropertyDescriptor(target, property).getWriteMethod().getName(), value);
	}

}
