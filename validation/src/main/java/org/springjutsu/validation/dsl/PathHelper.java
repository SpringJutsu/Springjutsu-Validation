package org.springjutsu.validation.dsl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.InvocationHandler;

/**
 * Will generate dynamic proxies along a call chain. Does not support
 * abstract/final types. Does not support classes with no default constructor.
 * Internally, overrides the toString method on each proxy to return the current path.
 * @author twicksell
 *
 */
public class PathHelper {
	
	public static <T> T forEntity(Class<T> clazz)
	{
		return forEntity(clazz, "", null);
	}
	
	protected static <T> T forEntity(Class<T> clazz, String path, Class parameterizedType)
	{
		Enhancer enhancer = new Enhancer();
		return (T) enhancer.create(clazz, new PathBuildingInvocationHandler(path, parameterizedType));
	}
	

}

class PathBuildingInvocationHandler extends PathHelper implements InvocationHandler
{

	String path;
	Class parameterizedType;
	public PathBuildingInvocationHandler(String path, Class parameterizedType) {
		this.path=path;
		this.parameterizedType=parameterizedType;
	}
	@Override
	public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
		Class returnType = (parameterizedType != null) ? parameterizedType : method.getReturnType();
		Class parameterType = null;
		if(method.getName().equals("toString"))
			return path;
		if(Collection.class.isAssignableFrom(method.getReturnType()))
		{
			parameterType = (Class) ((ParameterizedType)method.getGenericReturnType()).getActualTypeArguments()[0];
		}
	
		
		if(!path.isEmpty() && parameterizedType == null)
			path+=".";
		path+=StringUtils.uncapitalize(method.getName().replace("get", ""));
		if(method.getReturnType().equals(String.class))
			return path;
		if(Modifier.isFinal(method.getReturnType().getModifiers()))
			throw new RuntimeException("Cannot help with final or abstract return types: "+method.getReturnType());
		
		return PathHelper.forEntity(returnType, path, parameterType);
	}
	
}
