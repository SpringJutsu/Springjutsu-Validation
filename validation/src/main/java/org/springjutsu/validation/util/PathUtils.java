package org.springjutsu.validation.util;

import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.ReflectionUtils;

public class PathUtils {
	
	public static String appendPath(String... pathSegments) {
		String joinedPath = StringUtils.join(pathSegments, ".");
		return removeExtraneousPathSeparators(joinedPath);
	}
	
	/**
	 * Joins multiple path segments into a single path.
	 * @param pathSegments individual path segments
	 * @return joined path.
	 */
	public static String joinPathSegments(Collection<String> pathSegments) {
		String joinedPath = StringUtils.join(pathSegments, ".");
		return removeExtraneousPathSeparators(joinedPath);
	}
	
	public static String removeExtraneousPathSeparators(String path) {
		return path == null ? null : path.replaceAll("\\.+", "\\.").replaceAll("^\\.|\\.$", "").trim();
	}
	
	/**
	 * Determine if a path exists on the given class.
	 * @param clazz Class to check 
	 * @param path Path to check
	 * @return true if path exists.
	 */
	public static boolean pathExists(Class<?> clazz, String path) {
		Class<?> pathClass = getClassForPath(clazz, path, true);
		return pathClass != null;
	}
	
	/**
	 * Determine the class of the selected path on the target class. 
	 * @param clazz Class to check 
	 * @param path Path to check
	 * @param unwrapFinalCollectionType if true returns the parameterized collection type
	 * @return class for path.
	 */
	public static Class<?> getClassForPath(Class<?> clazz, String path, boolean unwrapFinalCollectionType) {
		Class<?>[] pathClasses = getClassesForPathTokens(clazz, path, unwrapFinalCollectionType);
		return pathClasses == null ? null : pathClasses[pathClasses.length - 1];
	}
	
	/**
	 * Determine the class of each step in the selected path on the target class. 
	 * @param clazz Class to check 
	 * @param path Path to check
	 * @param unwrapCollectionTypes if true returns the parameterized collection type
	 * @return array of classes for each step of path.
	 */
	public static Class<?>[] getClassesForPathTokens(Class<?> clazz, String path, boolean unwrapCollectionTypes) {
		if (path == null || path.trim().isEmpty()) {
			return null;
		}
		Class<?> intermediateClass = clazz;
		String[] pathTokens = path.split("\\.");
		Class<?>[] pathClasses = new Class<?>[pathTokens.length]; 
		
		for (int i = 0; i < pathTokens.length; i++) {
			String token = pathTokens[i];
			token = token.replaceAll("\\[[^\\]]+\\]$", "");
			PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(intermediateClass, token);
			if (descriptor == null) {
				return null;
			} else if (List.class.isAssignableFrom(descriptor.getPropertyType())) {
				intermediateClass = TypeDescriptor.nested(ReflectionUtils.findField(
								intermediateClass, token), 1).getObjectType();
			} else if (descriptor.getPropertyType().isArray()) {
				intermediateClass = descriptor.getPropertyType().getComponentType();
			} else {
				intermediateClass = descriptor.getPropertyType();
			}
			if (unwrapCollectionTypes) {
				pathClasses[i] = intermediateClass;
			} else {
				pathClasses[i] = descriptor.getPropertyType();
			}
		}
		return pathClasses;
	}
	
	/**
	 * Get a path constructed specified set of subpath segments from a path.
	 * @param path the original path
	 * @param startToken The first token index, inclusive: if null, the first token.
	 * @param endToken The last token index, inclusive: if null, the last token.
	 * @return The extracted subpath.
	 */
	public static String subPath(String path, Integer startToken, Integer endToken) {
		String[] tokens = path.trim().split("\\.");
		int firstToken = startToken == null ? 0 : startToken;
		int lastToken = endToken == null ? tokens.length - 1 : endToken;
		String subPath = null;
		for (int i = firstToken; i <= lastToken; i++) {
			subPath = appendPath(subPath, tokens[i]);
		}
		return subPath;
	}
	
	/**
	 * Use to determine if a path contains an ${EL} fragment.
	 * @param path Path to check for EL fragments
	 * @return true if the path contains an EL fragment.
	 */
	public static boolean containsEL(String path) {
		return path.matches(".*\\$\\{.+\\}.*");
	}
}
