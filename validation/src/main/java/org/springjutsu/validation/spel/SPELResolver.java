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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.TypeConverter;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * A customizable resolver of SPEL Expressions.
 * @author Clark Duplichien
 */
public class SPELResolver {
	
	public static final String EXPRESSION_MATCHER = "\\$\\{(.(?!\\$\\{))+\\}";
	
	/**
	 * Evaluation context which will contain 
	 * request handler implementation-specific scopes.
	 */
	protected NamedScopeEvaluationContext scopedContext;
	
	/**
	 * Expression parser used to parse expressions.
	 */
	protected ExpressionParser expressionParser;
	
	/**
	 * Core model object.
	 */
	protected Object model;
	
	/**
	 * Type converter loaded with optional Spring conversion service.
	 */
	protected TypeConverter typeConverter;
	
	/**
	 * Initialize evaluation context and expression parser.
	 * Initializes property accessors and contexts.
	 * @param model the Model for this request 
	 */
	public SPELResolver(Object model, TypeConverter typeConverter) {
		this.model = model;
		this.typeConverter = typeConverter;
		reset();
	}
	
	/**
	 * Resets SPEL resolver to an initial state,
	 * in order to clear any customization or contexts
	 * added by validation contexts, etc.
	 */
	public void reset() {
		scopedContext = new NamedScopeEvaluationContext();
		expressionParser = new SpelExpressionParser();
		
		// init named contexts
		scopedContext.addContext("model", model);
	}
	
	/**
	 * Evaluates a SPEL expression within the
	 * current web context, returning the result.
	 * @param spel String SPEL expression
	 * @return result of evaluated SPEL expression.
	 */
	public Object getBySpel(String spel) {
		Object spelResult = null;
		try {
			spelResult = expressionParser.parseExpression(spel).getValue(scopedContext);
			// TODO: pretty sure we can get around this expensive catch with an always-null property accessor.
		} catch (SpelEvaluationException see) {
			if (see.getMessage().contains("cannot be found")) {
				return null;
			} else {
				throw see;
			}
		}
		
		// if SPEL resolves into another SPEL expression,
		// that looks pretty sketchy, and would get run again,
		// so de-SPEL to prevent SPEL-injection vulnerability.
		if (spelResult instanceof String) {
			Matcher matcher = Pattern.compile(EXPRESSION_MATCHER).matcher((String) spelResult);
			while (matcher.find()) {
				String elString = matcher.group();
				String unSpelledSpel = elString.substring(2, elString.length() - 1);
				spelResult = ((String) spelResult).replace(elString, unSpelledSpel);
			}
		}
		
		return spelResult;
	}
	
	/**
	 * Resolves one or more SPEL expressions in the given string.
	 * @param elContainng A string potentially containing one or more SPEL expressions
	 * @return Either an object represented by the expression (if the entire string
	 *  was an expression) or a new String with all SPEL expressions replaced by
	 *  the string value of the respective resolved objects.
	 */
	public Object resolveSPELString(String elContaining) {
		// if the whole thing is a single EL string, try to get the object.
		if (elContaining.matches(EXPRESSION_MATCHER)) {
			String resolvableElString = 
				elContaining.substring(2, elContaining.length() - 1) + "?: null";
			Object elResult = getBySpel(resolvableElString);
			return elResult;
		} else {
			// otherwise, do string value substitution to build a value.
			String elResolvable = elContaining;
			Matcher matcher = Pattern.compile(EXPRESSION_MATCHER).matcher(elResolvable);
			while (matcher.find()) {
				String elString = matcher.group();
				String resolvableElString = 
						elString.substring(2, elString.length() - 1) + "?: null";
				Object elResult = getBySpel(resolvableElString);
				String resolvedElString = elResult != null ? 
					typeConverter.convertIfNecessary(elResult, String.class) : "";
				elResolvable = elResolvable.replace(elString, resolvedElString);
			}
			return elResolvable;
		}
	}
	
	/**
	 * Sets some object to the location specified by 
	 * a spel expression.
	 * @param spel String SPEL expression
	 * @param object some object to set at SPEL-specified location.
	 */
	public void setBySpel(String spel, Object object) {
		expressionParser.parseExpression(spel).setValue(scopedContext, object);		
	}

	public NamedScopeEvaluationContext getScopedContext() {
		return scopedContext;
	}

	public ExpressionParser getExpressionParser() {
		return expressionParser;
	}

}
