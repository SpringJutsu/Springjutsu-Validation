package org.springjutsu.validation.context;

import java.util.Set;

import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.expression.spel.FlowVariablePropertyAccessor;
import org.springjutsu.validation.spel.ReadCheckingMapAdaptablePropertyAccessor;
import org.springjutsu.validation.spel.SPELResolver;
import org.springjutsu.validation.util.RequestUtils;

/**
 * Handles validation contexts of type "webflow".
 * Determines whether or not a specific flow 
 * and state are currently active within a
 * webflow request context.
 * @author Clark Duplichien
 */
public class WebflowValidationContextHandler implements ValidationContextHandler {

	/**
	 * Will return true if the current request is a webflow
	 * request and the given qualifier in the form
	 * flowName:stateName matches the flow name
	 * and state name of the current web flow request.
	 */
	@Override
	public boolean isActive(Set<String> qualifiers) {
		if (!isWebflowRequest()) {
			return false;
		} else {
			return qualifiers.contains(getWebflowFormName());
		}
	}
	
	/**
	 * Gets a identifier of the current state that needs validating in
	 * order to determine what rules to load from the validation definition.
	 * For webflow, this is the flow ID appended with a colon, and then the 
	 * state id.
	 * For example /accounts/account-creation:basicInformation
	 * @return the context rules associated with this identifier.
	 */
	protected String getWebflowFormName() {
		StringBuffer flowStateId = new StringBuffer();
		flowStateId.append(RequestContextHolder.getRequestContext().getCurrentState().getOwner().getId());
		flowStateId.append(":");
		flowStateId.append(RequestContextHolder.getRequestContext().getCurrentState().getId());
		String flowStateIdString = RequestUtils.removeLeadingAndTrailingSlashes(flowStateId.toString());
		return flowStateIdString;
	}
	
	/**
	 * @return true if the current web request is associated
	 * with Spring Web Flow.
	 */
	public static boolean isWebflowRequest() {
		return RequestContextHolder.getRequestContext() != null;
	}

	/**
	 * Initialize the SPEL resolver with access to
	 * Spring Web Flow specific scopes.
	 */
	@Override
	public void initializeSPELResolver(SPELResolver spelResolver) {
		// init property accessors
		spelResolver.getScopedContext().addPropertyAccessor(new ReadCheckingMapAdaptablePropertyAccessor());
		spelResolver.getScopedContext().addPropertyAccessor(new FlowVariablePropertyAccessor());
		
		// init named contexts
		RequestContext requestContext = RequestContextHolder.getRequestContext();
		spelResolver.getScopedContext().addContext("requestScope", requestContext.getRequestScope());
		spelResolver.getScopedContext().addContext("flashScope", requestContext.getFlashScope());
		if (requestContext.inViewState()) {
			spelResolver.getScopedContext().addContext("viewScope", requestContext.getViewScope());
		}
		spelResolver.getScopedContext().addContext("flowScope", requestContext.getFlowScope());
		spelResolver.getScopedContext().addContext("conversationScope", requestContext.getConversationScope());
		spelResolver.getScopedContext().addContext("requestParameters", requestContext.getRequestParameters());
		spelResolver.getScopedContext().addContext("requestAttributes", requestContext.getExternalContext().getRequestMap());
		spelResolver.getScopedContext().addContext("session", requestContext.getExternalContext().getSessionMap());
	}
	
}
