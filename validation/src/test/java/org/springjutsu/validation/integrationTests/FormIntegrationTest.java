package org.springjutsu.validation.integrationTests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.Errors;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockExternalContext;
import org.springframework.webflow.test.MockRequestContext;
import org.springjutsu.validation.test.entities.Customer;

public class FormIntegrationTest extends ValidationIntegrationTest {
	
	@Test
	public void testMultiSourceMVCFormRules() {
		setCurrentFormPath("/foo/new");
		Customer customer = new Customer();
		Errors errors = doValidate("testFormRules.xml", customer).errors;
		assertEquals(2, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("firstName").getCode());
		assertEquals("errors.required", errors.getFieldError("lastName").getCode());
	}
	
	@Test
	public void testPathVariableMVCFormRules() {
		setCurrentFormPath("/foo/1/edit");
		Customer customer = new Customer();
		Errors errors = doValidate("testFormRules.xml", customer).errors;
		assertEquals(1, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("firstName").getCode());
	}
	
	@Test
	public void testWildcardPathMVCFormRules() {
		setCurrentFormPath("/bar/1/foo/new");
		Customer customer = new Customer();
		Errors errors = doValidate("testFormRules.xml", customer).errors;
		assertEquals(1, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("firstName").getCode());
	}
	
	@Test
	public void testWildcardAndPathVariableMVCFormRules() {
		setCurrentFormPath("/bar/1/foo/1/edit");
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
