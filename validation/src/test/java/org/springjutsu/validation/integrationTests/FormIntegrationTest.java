package org.springjutsu.validation.integrationTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.Errors;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockExternalContext;
import org.springframework.webflow.test.MockRequestContext;
import org.springjutsu.validation.test.entities.Customer;

public class FormIntegrationTest extends ValidationIntegrationTest {
	
	@Override
	protected String getXmlSubdirectory() {
		return "formIntegrationTest";
	}
	
	@Test
	public void testMultiSourceMVCFormRules() {
		setCurrentFormPath("/foo/new");
		Customer customer = new Customer();
		Errors errors = doValidate("testFormRules.xml", customer).errors;
		assertEquals(3, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("emailAddress").getCode());
		assertEquals("errors.required", errors.getFieldError("firstName").getCode());
		assertEquals("errors.required", errors.getFieldError("lastName").getCode());
	}
	
	@Test
	public void testPathVariableMVCFormRules() {
		setCurrentFormPath("/foo/1/edit");
		Customer customer = new Customer();
		Errors errors = doValidate("testFormRules.xml", customer).errors;
		assertEquals(2, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("emailAddress").getCode());
		assertEquals("errors.required", errors.getFieldError("firstName").getCode());
	}
	
	@Test
	public void testWildcardPathMVCFormRules() {
		setCurrentFormPath("/bar/1/foo/new");
		Customer customer = new Customer();
		Errors errors = doValidate("testFormRules.xml", customer).errors;
		assertEquals(2, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("emailAddress").getCode());
		assertEquals("errors.required", errors.getFieldError("firstName").getCode());
	}
	
	@Test
	public void testWildcardAndPathVariableMVCFormRules() {
		setCurrentFormPath("/bar/1/foo/1/edit");
		Customer customer = new Customer();
		Errors errors = doValidate("testFormRules.xml", customer).errors;
		assertEquals(2, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("emailAddress").getCode());
		assertEquals("errors.required", errors.getFieldError("firstName").getCode());
	}
	
	@Test
	public void testWebFlowFormRules() {
		RequestContext mockRequestContext = new MockRequestContext();
		((MockExternalContext) mockRequestContext.getExternalContext()).setNativeRequest(new MockHttpServletRequest());
		org.springframework.webflow.execution.RequestContextHolder.setRequestContext(mockRequestContext);
		Customer customer = new Customer();
		Errors errors = doValidate("testFormRules.xml", customer).errors;
		assertEquals(2, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("emailAddress").getCode());
		assertEquals("errors.required", errors.getFieldError("firstName").getCode());	
	}
	
	@Test
	public void testSubPathRulesIgnoreFormRules() {
		setCurrentFormPath("/foo/new");
		Customer customer = new Customer();
		customer.setFirstName("bob");
		customer.setReferredBy(new Customer());
		customer.getReferredBy().setFirstName("bob");
		Errors errors = doValidate("testSubPathFormRulesIgnored.xml", customer).errors;
		assertEquals(2, errors.getErrorCount());
		assertNull(errors.getFieldError("firstName"));
		assertEquals("errors.required", errors.getFieldError("lastName").getCode());
		assertEquals("errors.required", errors.getFieldError("emailAddress").getCode());
	}

}
