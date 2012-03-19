package org.springjutsu.examples.notimportant;

import javax.servlet.http.HttpServletRequest;

import org.springframework.webflow.context.servlet.DefaultFlowUrlHandler;

public class TrailingSlashesShouldntBreakWebflowUrlHandler extends DefaultFlowUrlHandler {
	
	@Override
	public String getFlowId(HttpServletRequest request) {
		String flowId = super.getFlowId(request);
		if (flowId.endsWith("/")) {
			flowId = flowId.substring(0, flowId.length() - 1);
		}
		return flowId;
	}

}
