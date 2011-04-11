
package org.springjutsu.validation.spel;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.*;

import org.junit.Test;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;

public class NamedScopeEvaluationContextTest {
	
	@Test
	public void testContextEvaluation() {
		Integer firstContext = new Integer("5");
		
		Map<String, Object> secondContext = new HashMap<String, Object>();
		secondContext.put("class", "math");
		secondContext.put("carmen", "sandiego");
		
		Map<String, Object> thirdContext = new HashMap<String, Object>();
		thirdContext.put("class", "upper");
		thirdContext.put("carmen", "Don't know her.");
		
		NamedScopeEvaluationContext context = new NamedScopeEvaluationContext();
		context.addContext("first", firstContext);
		context.addContext("second", secondContext);
		context.addContext("third", thirdContext);
		
		ExpressionParser expressionParser = new SpelExpressionParser();
		
		// test find context by name.
		assertEquals(5, expressionParser.parseExpression("first").getValue(context));
		// test find object on first context in which it exists.
		assertEquals(Integer.class, expressionParser.parseExpression("class").getValue(context));
		// test find object on specific named context.
		assertEquals("math", expressionParser.parseExpression("second.class").getValue(context));
		// test find object on specific named context.
		assertEquals("upper", expressionParser.parseExpression("third.class").getValue(context));
		// test find object on first context in which it exists.
		assertEquals("sandiego", expressionParser.parseExpression("carmen").getValue(context));
		// test find object on specific named context.
		assertEquals("Don't know her.", expressionParser.parseExpression("third.carmen").getValue(context));
		
		// test to ensure that property access still explodes when it should.
		boolean spelExceptionCaught = false;
		try {
			System.out.println(expressionParser.parseExpression("first.carmen").getValue(context));
		} catch (SpelEvaluationException see) {
			spelExceptionCaught = true;
		} finally {
			assertTrue("\"carmen\" should NOT be readable on first context...", spelExceptionCaught);
		}
	}

}
