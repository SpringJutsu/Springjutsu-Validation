package org.springjutsu.validation.integrationTests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.Errors;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockExternalContext;
import org.springframework.webflow.test.MockRequestContext;
import org.springjutsu.validation.test.entities.Customer;

public class FormIntegrationTest extends ValidationIntegrationTest {
	
	@Test
	public void testMultiSourceMVCFormRules() {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/foo/new");
		request.setServletPath("/foo/new");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request), true);
		Customer customer = new Customer();
		Errors errors = doValidate("testFormRules.xml", customer).errors;
		assertEquals(2, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("firstName").getCode());
		assertEquals("errors.required", errors.getFieldError("lastName").getCode());
	}
	
	@Test
	public void testPathVariableMVCFormRules() {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/foo/1/edit");
		request.setServletPath("/foo/1/edit");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request), true);
		Customer customer = new Customer();
		Errors errors = doValidate("testFormRules.xml", customer).errors;
		assertEquals(1, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("firstName").getCode());
	}
	
	@Test
	public void testWildcardPathMVCFormRules() {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/bar/1/foo/new");
		request.setServletPath("/bar/1/foo/new");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request), true);
		Customer customer = new Customer();
		Errors errors = doValidate("testFormRules.xml", customer).errors;
		assertEquals(1, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("firstName").getCode());
	}
	
	@Test
	public void testWildcardAndPathVariableMVCFormRules() {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/bar/1/foo/1/edit");
		request.setServletPath("/bar/1/foo/1/edit");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request), true);
		Customer customer = new Customer();
		Errors errors = doValidate("testFormRules.xml", customer).errors;
		assertEquals(1, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("firstName").getCode());
	}
	
	@Test
	public void testWebFlowFormRules() {
		RequestContext mockRequestContext = new MockRequestContext();
		((MockExternalContext) mockRequestContext.getExternalContext()).setNativeRequest(new MockHttpServletRequest());
		org.springframework.webflow.execution.RequestContextHolder.setRequestContext(mockRequestContext);
		Customer customer = new Customer();
		Errors errors = doValidate("testFormRules.xml", customer).errors;
		assertEquals(1, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("firstName").getCode());	
	}

}
