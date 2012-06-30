package org.springjutsu.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Locale;

import org.junit.After;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.Errors;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockExternalContext;
import org.springframework.webflow.test.MockRequestContext;
import org.springjutsu.validation.rules.ValidationRulesContainer;
import org.springjutsu.validation.test.entities.Address;
import org.springjutsu.validation.test.entities.Company;
import org.springjutsu.validation.test.entities.Customer;
import org.springjutsu.validation.test.entities.Developer;
import org.springjutsu.validation.test.entities.Person;
import org.springjutsu.validation.test.entities.ValuedCustomer;

public class ValidationIntegrationTest {
	
	private static final String xmlDirectory = 
		"org/springjutsu/validation/integration/";
	
	protected TestResult doValidate(String configXml, Object validateMe) {
		ApplicationContext context =
		    new ClassPathXmlApplicationContext(new String[] {
		    	xmlDirectory + configXml});
		ValidationManager manager = context.getBean(ValidationManager.class);
		return new TestResult(manager.validate(validateMe), context.getBean(MessageSource.class));
	}
	
	protected class TestResult {
		public Errors errors;
		public MessageSource messageSource;
		public TestResult(Errors errors, MessageSource messageSource) {
			this.errors = errors;
			this.messageSource = messageSource;
		}
	}
	
	@After
	public void cleanupRequests() {
		RequestContextHolder.setRequestAttributes(null);
		org.springframework.webflow.execution.RequestContextHolder.setRequestContext(null);
	}
	
	@Test
	public void testBasicRules() {
		Errors errors = doValidate("testBasicRules.xml", new Customer()).errors;
		assertEquals(3, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("firstName").getCode());
		assertEquals("errors.required", errors.getFieldError("lastName").getCode());
		assertEquals("errors.required", errors.getFieldError("emailAddress").getCode());
	}
	
	@Test
	public void testNestedRules() {
		Customer customer = new Customer();
		customer.setFirstName("bob");
		Errors errors = doValidate("testNestedRules.xml", customer).errors;
		assertEquals(2, errors.getErrorCount());
		assertNull(errors.getFieldError("firstName"));
		assertEquals("errors.required", errors.getFieldError("lastName").getCode());
		assertEquals("errors.required", errors.getFieldError("emailAddress").getCode());
	}
	
	@Test
	public void testPreventRuleRecursion() {
		Customer customer = new Customer();
		customer.setReferredBy(customer);
		Address address = new Address();
		address.setCustomer(customer);
		customer.setAddress(address);
		customer.setSecondaryAddress(address);
		
		Errors errors = doValidate("testPreventRuleRecursion.xml", customer).errors;
		assertEquals(3, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("firstName").getCode());
		assertNull(errors.getFieldError("referredBy.firstName"));
		assertEquals("errors.required", errors.getFieldError("address.city").getCode());
		assertNull(errors.getFieldError("address.customer.firstName"));
		assertEquals("errors.required", errors.getFieldError("secondaryAddress.state").getCode());
		assertNull(errors.getFieldError("secondaryAddress.customer.firstName"));
	}
	
	@Test
	public void testCollectionRules() {
		Company company = new Company();
		Customer namedCustomer = new Customer();
		namedCustomer.setFirstName("bob");
		company.getCustomers().add(namedCustomer);
		company.getCustomers().add(new Customer());
		company.getCustomers().add(new Customer());
		
		Errors errors = doValidate("testCollectionRules.xml", company).errors;
		assertEquals(3, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("name").getCode());
		assertNull(errors.getFieldError("customers[0].firstName"));
		assertEquals("errors.required", errors.getFieldError("customers[1].firstName").getCode());
		assertEquals("errors.required", errors.getFieldError("customers[2].firstName").getCode());
	}
	
	@Test
	public void testSPELvsModelBean() {
		Customer customer = new Customer();
		customer.setFirstName("bob");
		customer.setLastName("notBob");
		Errors errors = doValidate("testSPELvsModelBean.xml", customer).errors;
		assertEquals(2, errors.getErrorCount());
		assertEquals("errors.matches", errors.getFieldError("firstName").getCode());
		assertEquals("errors.matches", errors.getFieldError("lastName").getCode());
	}
	
	@Test
	public void testDefaultMessageNegotiation() {
		Customer customer = new Customer();
		customer.setFirstName("bob");
		customer.setLastName("joe");
		TestResult result = doValidate("testDefaultMessageNegotiation.xml", customer);
		Errors errors = result.errors;
		MessageSource messageSource = result.messageSource;
		assertEquals(3, errors.getErrorCount());
		assertEquals("First Name must match Last Name", messageSource.getMessage(errors.getFieldError("firstName"), Locale.US));
		assertEquals("Last Name must be at least 4 characters long", messageSource.getMessage(errors.getFieldError("lastName"), Locale.US));
		assertEquals("emailAddress required", messageSource.getMessage(errors.getFieldError("emailAddress"), Locale.US));
	}
	
	@Test
	public void testCustomMessageNegotiation() {
		Customer customer = new Customer();
		customer.setFirstName("bob");
		customer.setLastName("joe");
		TestResult result = doValidate("testCustomMessageNegotiation.xml", customer);
		Errors errors = result.errors;
		MessageSource messageSource = result.messageSource;
		assertEquals(3, errors.getErrorCount());
		assertEquals("First Name must match Last Name", messageSource.getMessage(errors.getFieldError("firstName"), Locale.US));
		assertEquals("Last Name must be at least 4 characters long", messageSource.getMessage(errors.getFieldError("lastName"), Locale.US));
		assertEquals("emailAddress required", messageSource.getMessage(errors.getFieldError("emailAddress"), Locale.US));
	}
	
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
	
	@Test
	public void testRuleWithNoPath() {
		Customer customer = new Customer();
		Errors errors = doValidate("testRuleWithNoPath.xml", customer).errors;
		assertEquals(1, errors.getErrorCount());
		assertEquals("errors.alphabetic", errors.getGlobalError().getCode());
	}
	
	@Test
	public void testValidationTemplates() {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/foo/new");
		request.setServletPath("/foo/new");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request), true);
		Customer customer = new Customer();
		Errors errors = doValidate("testValidationTemplates.xml", customer).errors;
		assertEquals(3, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("address.lineOne").getCode());
		assertEquals("errors.required", errors.getFieldError("address.city").getCode());
		assertEquals("errors.required", errors.getFieldError("secondaryAddress.city").getCode());
	}
	
	@Test
	public void testRuleInheritance() {
		ApplicationContext context =
		    new ClassPathXmlApplicationContext(new String[] {
		    	xmlDirectory + "testInheritance.xml"});
		ValidationRulesContainer container = context.getBean(ValidationRulesContainer.class);
		assertEquals(2, container.getValidationEntity(Person.class).getRules().size());
		assertEquals(3, container.getValidationEntity(Developer.class).getRules().size());
		assertEquals(3, container.getValidationEntity(Customer.class).getRules().size());
		assertEquals(4, container.getValidationEntity(ValuedCustomer.class).getRules().size());
	}
	
	/**
	 * Test fix for bug #14
	 */
	public void testInitializationDelegation() {
		ApplicationContext context =
		    new ClassPathXmlApplicationContext(new String[] {
		    	xmlDirectory + "testInheritance.xml"});
		ValidationRulesContainer container = context.getBean(ValidationRulesContainer.class);
		container.supportsClass(ValuedCustomer.class);
		assertEquals(4, container.getValidationEntity(ValuedCustomer.class).getRules().size());
	}
	
	/**
	 * Test fix for bug #14
	 */
	public void testInitializationDelegationAgain() {
		ApplicationContext context =
		    new ClassPathXmlApplicationContext(new String[] {
		    	xmlDirectory + "testInheritance.xml"});
		ValidationRulesContainer container = context.getBean(ValidationRulesContainer.class);
		container.hasRulesForClass(ValuedCustomer.class);
		assertEquals(4, container.getValidationEntity(ValuedCustomer.class).getRules().size());
	}
	
	@Test
	public void testCollectionRulePathFromModel() {
		Company company = new Company();
		Customer namedCustomer = new Customer();
		namedCustomer.setFirstName("bob");
		company.getCustomers().add(namedCustomer);
		company.getCustomers().add(new Customer());
		company.getCustomers().add(new Customer());
		
		Errors errors = doValidate("testCollectionRulePathFromModel.xml", company).errors;
		assertEquals(3, errors.getErrorCount());
		assertEquals("errors.required", errors.getFieldError("name").getCode());
		assertNull(errors.getFieldError("customers[0].firstName"));
		assertEquals("errors.required", errors.getFieldError("customers[1].firstName").getCode());
		assertEquals("errors.required", errors.getFieldError("customers[2].firstName").getCode());
	}
	
	@Test
	public void testCollectionRulePathFromCollectionNestedModel() {
		Company company = new Company();
		Customer namedCustomer = new Customer();
		namedCustomer.setFirstName("bob");
		company.getAcquisitions().add(new Company());
		company.getAcquisitions().add(new Company());
		company.getAcquisitions().get(0).getCustomers().add(new Customer());
		company.getAcquisitions().get(0).getCustomers().add(new Customer());
		company.getAcquisitions().get(1).getCustomers().add(namedCustomer);
		company.getAcquisitions().get(1).getCustomers().add(new Customer());
		
		Errors errors = doValidate("testCollectionRulePathFromCollectionNestedModel.xml", company).errors;
		assertEquals(3, errors.getErrorCount());
		
		assertEquals("errors.required", errors.getFieldError("acquisitions[0].customers[0].firstName").getCode());
		assertEquals("errors.required", errors.getFieldError("acquisitions[0].customers[1].firstName").getCode());
		assertNull(errors.getFieldError("acquisitions[1].customers[0].firstName"));
		assertEquals("errors.required", errors.getFieldError("acquisitions[1].customers[1].firstName").getCode());
	}
	
	@Test
	public void testNestedCollectionRulePathFromModel() {
		Company company = new Company();
		Customer namedCustomer = new Customer();
		namedCustomer.setFirstName("bob");
		company.getAcquisitions().add(new Company());
		company.getAcquisitions().add(new Company());
		company.getAcquisitions().get(0).getCustomers().add(new Customer());
		company.getAcquisitions().get(0).getCustomers().add(new Customer());
		company.getAcquisitions().get(1).getCustomers().add(namedCustomer);
		company.getAcquisitions().get(1).getCustomers().add(new Customer());
		
		Errors errors = doValidate("testNestedCollectionRulePathFromModel.xml", company).errors;
		assertEquals(3, errors.getErrorCount());
		
		assertEquals("errors.required", errors.getFieldError("acquisitions[0].customers[0].firstName").getCode());
		assertEquals("errors.required", errors.getFieldError("acquisitions[0].customers[1].firstName").getCode());
		assertNull(errors.getFieldError("acquisitions[1].customers[0].firstName"));
		assertEquals("errors.required", errors.getFieldError("acquisitions[1].customers[1].firstName").getCode());
	}
}
