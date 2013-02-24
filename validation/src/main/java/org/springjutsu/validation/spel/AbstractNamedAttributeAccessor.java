package org.springjutsu.validation.spel;

/**
 * A happy helper for accessing attributes on
 * a class that looks like a map, and smells like a map
 * but isn't really a map, but could have map-style access.
 * Used in combination with the NamedAttributeAccessorPropertyAccessor,
 * this allows map-like objects to be exposed as named contexts
 * to the NamedScopeEvaluationContext.
 * @author Clark Duplichien
 *
 */
public abstract class AbstractNamedAttributeAccessor {
	
	/**
	 * Indicates if this map-like object
	 * supports writing values
	 * @return true if objects can be written.
	 */
	public boolean isWritable() {
		return true;
	}
	
	/**
	 * An awfully map-like get method.
	 * @param key The key to get a value from
	 * @return the value associated with the key
	 */
	public abstract Object get(String key);
	
	/**
	 * An set method much like that of a map's.
	 * @param key The key under which the 
	 * value should be stored
	 * @param value The value to store under the key
	 */
	public abstract void set(String key, Object value);

}
