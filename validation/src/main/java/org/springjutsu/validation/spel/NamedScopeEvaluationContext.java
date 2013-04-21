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

import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.AccessException;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.ExpressionState;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.ast.PropertyOrFieldReference;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Acts as Multiple SPEL Evaluation Contexts from a single instance.
 * Allows multiple "scopes" to each be given a name.
 * When searching for properties within the Evaluation Context, the
 * named scopes will be searched in the order they were registered.
 * If the property being sought is qualified with the scope name, then
 * the appropriate scope will be directly accessed.
 * This allows this class to:
 * 1) Be a work around for the reversed order of class-matching property
 * accessors, reported in SPR-8211, and
 * 2) Allow the creation of multiple named contexts without having to create
 * a dedicated class with named fields for each of the scopes, and
 * 3) Allows for full use of property accessors against each registered scope.
 * @author Clark Duplichien
 *
 */
public class NamedScopeEvaluationContext extends StandardEvaluationContext {
	
	/**
	 * The multiple scopes to search at property access time.
	 * I couldn't decide whether to call it "context"
	 * or "scope", so here it's called both. This indecision 
	 * is prevalent throughout the implementation. Sorry.
	 */
	ContextScope contextScope;

	/**
	 * Creates an empty instance, on which scopes can be registered.
	 * By ensuring a @see{MapAccessor} is registered, we allow the
	 * hack that is the @see{ContextScope} class to work.
	 */
	public NamedScopeEvaluationContext() {
		contextScope = new ContextScope();
		this.setRootObject(contextScope);
		this.addPropertyAccessor(new MapAccessor());
	}
	
	/**
	 * Adds a Scope, or Context, however you term it.
	 * @param contextName The name of the context / scope.
	 * @param context The context / scope object itself.
	 */
	public void addContext(String contextName, Object context) {
		contextScope.put(contextName, context);
	}
	
	/**
	 * This is the container for Multiple Named Scopes.
	 * It inherits from an ordered-access map, each attempt to
	 * access a property in the map will first check for a 
	 * context with the name provided in the key. If none exists,
	 * we then go on to check each scope in turn for the presence
	 * of that property, utilizing all @see{PropertyAccessor} 
	 * instances registered to this EvaluationContext. Sweet.
	 * @author Clark Duplichien
	 */
	class ContextScope extends LinkedHashMap<String, Object> {
		
		private static final long serialVersionUID = 1L;
		
		/**
		 * If a given property is found within a specific context,
		 * we cache the property name and the context it's found in.
		 * Just allows for better performance / less redundant searching.
		 * Since this whole instance is a thread-specific throwaway, there's
		 * no cap on the cache size. Shouldn't be a problem since the max
		 * size is limited by how many properties a user can actually write
		 * validation rules for on a single form.
		 */
		LinkedHashMap<String, ExpressionState> stateCache;
		
		/**
		 * Constructor initializes the state cache.
		 */
		ContextScope() {
			stateCache = new LinkedHashMap<String, ExpressionState>();
		}
		
		/**
		 * 1) Ignore null, we ain't got it. 
		 * 2) Check within the actual map, key might be the name of a scope.
		 * 3) See if the key describes a readable property within the scopes.
		 */
		@Override
		public boolean containsKey(Object key) {
			if (key == null) {
				return false;
			} else if (super.containsKey(key)) {
				return true;
			} else {
				return getReadableState(key) != null;
			}
		}
		
		/**
		 * Same behavior described in containsKey,
		 * except that we return the result. 
		 * We unwrap the result in case it was an 
		 * expressison state itself.
		 */
		@Override
		public Object get(Object key) {
			if (key == null) {
				return null;
			}
			if (super.containsKey(key)) {
				return unwrapExpressionState(super.get(key));
			} else if (this.containsKey(key)) {
				ExpressionState expressionState = getReadableState(key);
				PropertyOrFieldReference propRef = 
					new PropertyOrFieldReference(true, key.toString(), 1);
				return unwrapExpressionState(propRef.getValue(expressionState));
			} else {
				return null;
			}
		}
		
		/**
		 * Since our map implementation actually stores
		 * ExpressionStates as values, this can lead to some potentially
		 * curious cases where specific field access may be multiply 
		 * wrapped ExpressionStates.
		 * Or so I thought at the time.... May need to check if it's 
		 * really necessary to iterate in this method. 
		 * @param potentialExpressionState Object that might be an ExpressionState
		 * @return Object that is certainly not an ExpressionState instance
		 */
		protected Object unwrapExpressionState(Object potentialExpressionState) {
			Object object = potentialExpressionState;
			while (object instanceof ExpressionState) {
				object = ((ExpressionState) object).getRootContextObject().getValue();
			}
			return object;
		}
		
		/**
		 * Basically, we need to determine if any of the scopes 
		 * or contexts contain the property named by key. Since
		 * we don't really know what each of the contexts actually
		 * is in terms of implementation, we test each one using the
		 * property accessors registered to this EvaluationContext.
		 * The first state from which the property can be successfully 
		 * read using an accessor is the readable state.
		 * This should work fine unless a property accessor were to 
		 * return an incorrect value from its canRead method (SWF-1472).
		 * @param key The name of the property to discover.
		 * @return ExpressionState from which the property may be read.
		 */
		protected ExpressionState getReadableState(Object key) {
			if (stateCache.containsKey(key)) {
				return stateCache.get(key);
			} 
			
			for (String superKey : super.keySet()) {
				ExpressionState state = (ExpressionState) super.get(superKey);
				List<PropertyAccessor> propertyAccessors = state.getPropertyAccessors();
				for (PropertyAccessor accessor : propertyAccessors) {
					try {
						boolean accessorApplicable = false;
						if (accessor.getSpecificTargetClasses() == null
							|| accessor.getSpecificTargetClasses().length < 1) {
							accessorApplicable = true;
						} else {
							for (Class<?> clazz : accessor.getSpecificTargetClasses()) {
								if (clazz.isAssignableFrom(
										state.getRootContextObject().getValue().getClass())) {
									accessorApplicable = true;
									break;
								}
							}
						}
						if (accessorApplicable && accessor.canRead(state.getEvaluationContext(), 
									state.getRootContextObject().getValue(), key.toString())) {
							stateCache.put(key.toString(), state);
							return state;
						}						
					} catch (AccessException ae) {
						continue;
					}
				}
			}
			return null;
		}
		
		/**
		 * Store an ExpressionState version of the passed in context.
		 * Return an unwrapped version of the previous context.
		 */
		@Override
		public Object put(String key, Object value) {
			Object previousValue = super.put(key, 
					new ExpressionState(NamedScopeEvaluationContext.this, new TypedValue(value),
							new SpelParserConfiguration(false, false)));
			return unwrapExpressionState(previousValue);
		}
	}
}
