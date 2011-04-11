/*
 * Copyright 2010-2011 Duplichien, Wicksell, Springjutsu.org
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

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.expression.spel.BeanFactoryPropertyAccessor;
import org.springframework.webflow.expression.spel.FlowVariablePropertyAccessor;
import org.springframework.webflow.expression.spel.MapAdaptablePropertyAccessor;
import org.springframework.webflow.expression.spel.MessageSourcePropertyAccessor;
import org.springframework.webflow.test.MockRequestContext;
import org.springjutsu.validation.util.RequestUtils;

/**
 * A request-aware resolver of SPEL Expressions.
 * Evaluates SPEL expressions in a MVC & Web Flow aware context.
 * @author Clark Duplichien
 */
public class WebContextSPELResolver {
	
	/**
	 * Evaluation context which will contain 
	 * request handler implementation-specific scopes.
	 */
	NamedScopeEvaluationContext scopedContext;
	
	/**
	 * Expression parser used to parse expressions.
	 */
	ExpressionParser expressionParser;
	
	/**
	 * Initialize evaluation context and expression parser.
	 * Initializes property accessors and contexts.
	 * @param model the Model for this request 
	 */
	public WebContextSPELResolver(Object model) {
		scopedContext = new NamedScopeEvaluationContext();
		expressionParser = new SpelExpressionParser();
		initPropertyAccessors();
		initContexts(model);
	}
	
	/**
	 * The useful part. Evaluates a SPEL expression within the
	 * current web context.
	 * @param spel String SPEL expression
	 * @return result of evaluated SPEL expression.
	 */
	public Object resolveSpel(String spel) {
		try {
			return expressionParser.parseExpression(spel).getValue(scopedContext);
			// TODO: pretty sure we can get around this expensive catch with an always-null property accessor.
		} catch (SpelEvaluationException see) {
			if (see.getMessage().contains("cannot be found")) {
				return null;
			} else {
				throw see;
			}
		}
	}
	
	/**
	 * Either gets the current thread-bound Web Flow requestContext
	 * instance, if this is a Web Flow request, or mocks a Web Flow 
	 * request out of an MVC request, in order to standardize 
	 * our web-scoped property access.
	 * @return RequestContext instance
	 */
	protected RequestContext getOrMockRequestContext() {
		RequestContext requestContext;	
		if (RequestUtils.isWebflowRequest()) {
			requestContext = RequestContextHolder.getRequestContext();
		} else {
			requestContext = new MockRequestContext();
			ServletRequestAttributes requestAttributes = (ServletRequestAttributes) 
				org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
			ExternalContext externalContext = 
				new ServletExternalContext(null, requestAttributes.getRequest(), null);
			((MockRequestContext) requestContext).setExternalContext(externalContext);
		}
		return requestContext;
	}
	
	/**
	 * Initialize property accessors.
	 * Only add Web Flow variables if this is a Web Flow request.
	 */
	protected void initPropertyAccessors() {
		scopedContext.addPropertyAccessor(new ReadCheckingMapAdaptablePropertyAccessor());
		scopedContext.addPropertyAccessor(new MessageSourcePropertyAccessor());
		if (RequestUtils.isWebflowRequest()) {
			scopedContext.addPropertyAccessor(new FlowVariablePropertyAccessor());
		}
		scopedContext.addPropertyAccessor(new BeanFactoryPropertyAccessor());
	}
	
	/**
	 * Initialize Scopes to search within the Named Scope Context.
	 * Only add Web Flow Scopes if this is a Web Flow Request.
	 * @param model The validated model for this request.
	 */
	protected void initContexts(Object model) {
		RequestContext requestContext = getOrMockRequestContext();
		scopedContext.addContext("model", model);
		if (RequestUtils.isWebflowRequest()) {
			scopedContext.addContext("requestScope", requestContext.getRequestScope());
			scopedContext.addContext("flashScope", requestContext.getFlashScope());
			scopedContext.addContext("viewScope", requestContext.getViewScope());
			scopedContext.addContext("flowScope", requestContext.getFlowScope());
			scopedContext.addContext("conversationScope", requestContext.getConversationScope());
		}
		scopedContext.addContext("requestParameters", requestContext.getRequestParameters());
		scopedContext.addContext("requestAttributes", requestContext.getExternalContext().getRequestMap());
		scopedContext.addContext("session", requestContext.getExternalContext().getSessionMap());
	}

}
