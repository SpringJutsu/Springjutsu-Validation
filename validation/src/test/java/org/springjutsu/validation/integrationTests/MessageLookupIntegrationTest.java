package org.springjutsu.validation.integrationTests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.Errors;
import org.springjutsu.validation.test.entities.Customer;
import org.springjutsu.validation.util.RequestUtils;

public class MessageLookupIntegrationTest extends ValidationIntegrationTest {
	
	@Override
	protected String getXmlSubdirectory() {
		return "messageLookupIntegrationTest";
	}
	
	@Test
	public void testMessageLookupGivenRuleNameErrorCodeWithNoPrefix() {
		Errors errors = doValidate("testMessageLookupGivenRuleNameErrorCodeWithNoPrefix.xml", new Customer()).errors;
		assertEquals(1, errors.getErrorCount());
		assertEquals("Got Unprefixed Error Message", errors.getFieldError("firstName").getDefaultMessage());
		assertEquals("messageOverride.required", errors.getFieldError("firstName").getCode());
	}
	
	@Test
	public void testMessageLookupGivenRuleNameErrorCodeWithPrefix() {
		Errors errors = doValidate("testMessageLookupGivenRuleNameErrorCodeWithPrefix.xml", new Customer()).errors;
		assertEquals(1, errors.getErrorCount());
		assertEquals("Got Prefixed Error Message", errors.getFieldError("firstName").getDefaultMessage());
		assertEquals("messageOverride.myprefix.required", errors.getFieldError("firstName").getCode());
	}
	
	@Test
	public void testMessageLookupGivenCustomMessageCode() {
		Errors errors = doValidate("testMessageLookupGivenCustomMessageCode.xml", new Customer()).errors;
		assertEquals(1, errors.getErrorCount());
		assertEquals("Got Custom Message Code", errors.getFieldError("firstName").getDefaultMessage());
		assertEquals("messageOverride.customMessageCode.basicLookup", errors.getFieldError("firstName").getCode());
	}
	
	@Test
	public void testMessageLookupGivenCustomMessageText() {
		Errors errors = doValidate("testMessageLookupGivenCustomMessageText.xml", new Customer()).errors;
		assertEquals(1, errors.getErrorCount());
		assertEquals("Got Custom Message Text", errors.getFieldError("firstName").getDefaultMessage());
		assertEquals("messageOverride.errorMessageTextProvided", errors.getFieldError("firstName").getCode());
	}
	
	@Test
	public void testMessageLookupGivenCustomMessageTextOverridesCustomMessageCode() {
		Errors errors = doValidate("testMessageLookupGivenCustomMessageTextOverridesCustomMessageCode.xml", new Customer()).errors;
		assertEquals(1, errors.getErrorCount());
		assertEquals("Got Custom Message Text", errors.getFieldError("firstName").getDefaultMessage());
		assertEquals("messageOverride.errorMessageTextProvided", errors.getFieldError("firstName").getCode());
	}
	
	@Test(expected=IllegalStateException.class)
	public void testMessageLookupGivenSPELPathWithoutErrorPath() {
		doValidate("testMessageLookupGivenSPELPathWithoutErrorPath.xml", new Customer());
	}
	
	@Test
	public void testLookupFailDefaultErrorDefaultsToPathAndRuleType() {
		Errors errors = doValidate("testLookupFailDefaultErrorDefaultsToPathAndRuleType.xml", new Customer()).errors;
		assertEquals(1, errors.getErrorCount());
		assertEquals("firstName required", errors.getFieldError("firstName").getDefaultMessage());
		assertEquals("messageOverride.aintchagonnafindit.required", errors.getFieldError("firstName").getCode());
	}
	
	@Test
	public void testLookupFailDefaultErrorGivenErrorMessageCodeDefaultsToMessageCode() {
		Errors errors = doValidate("testLookupFailDefaultErrorGivenErrorMessageCodeDefaultsToMessageCode.xml", new Customer()).errors;
		assertEquals(1, errors.getErrorCount());
		assertEquals("aintchagonnafindit.nawyanevagonnafindit", errors.getFieldError("firstName").getDefaultMessage());
		assertEquals("messageOverride.aintchagonnafindit.nawyanevagonnafindit", errors.getFieldError("firstName").getCode());
	}
	
	@Test
	public void testNumberedArgumentReplacementGivenRuleNameErrorCode() {
		Errors errors = doValidate("testNumberedArgumentReplacementGivenRuleNameErrorCode.xml", new Customer()).errors;
		assertEquals(1, errors.getErrorCount());
		assertEquals("(1) Model arg: First Name Lookup Successful, Value arg: Literal Value Lookup Successful", 
			errors.getFieldError("firstName").getDefaultMessage());
		assertEquals("messageOverride.numberedArgumentCheck1.matches", errors.getFieldError("firstName").getCode());
	}
	
	@Test
	public void testNumberedArgumentReplacementGivenErrorMessageCode() {
		Errors errors = doValidate("testNumberedArgumentReplacementGivenErrorMessageCode.xml", new Customer()).errors;
		assertEquals(1, errors.getErrorCount());
		assertEquals("(2) Model arg: First Name Lookup Successful, Value arg: Literal Value Lookup Successful", 
			errors.getFieldError("firstName").getDefaultMessage());
		assertEquals("messageOverride.numberedArgumentCheck2.matches", errors.getFieldError("firstName").getCode());
	}
	
	@Test
	public void testNumberedArgumentReplacementGivenErrorMessageText() {
		Errors errors = doValidate("testNumberedArgumentReplacementGivenErrorMessageText.xml", new Customer()).errors;
		assertEquals(1, errors.getErrorCount());
		assertEquals("(3) Model arg: First Name Lookup Successful, Value arg: Literal Value Lookup Successful", 
			errors.getFieldError("firstName").getDefaultMessage());
		assertEquals("messageOverride.errorMessageTextProvided", errors.getFieldError("firstName").getCode());
	}
	
	@Test
	public void testNumberedArgumentReplacementForSPELNotatedNonModelObject() {
		Errors errors = doValidate("testNumberedArgumentReplacementForSPELNotatedNonModelObject.xml", new Customer()).errors;
		assertEquals(1, errors.getErrorCount());
		assertEquals("(4) Model arg: Literal SPEL Model Lookup Successful, Value arg: Literal SPEL Value Lookup Successful", 
			errors.getFieldError("firstName").getDefaultMessage());
		assertEquals("messageOverride.errorMessageTextProvided", errors.getFieldError("firstName").getCode());
	}
	
	@Test
	public void testNumberedArgumentReplacmentForSPELNotatedModelPath() {
		Customer customer = new Customer();
		customer.setFirstName("thiswontmatchthat");
		customer.setLastName("thatwontmatchthis");
		Errors errors = doValidate("testNumberedArgumentReplacementForSPELNotatedModelPath.xml", customer).errors;
		assertEquals(1, errors.getErrorCount());
		assertEquals("(5) Model arg: SPEL Model Path Model Arg Lookup Successful, Value arg: SPEL Model Path Value Arg Lookup Successful", 
			errors.getFieldError("firstName").getDefaultMessage());
		assertEquals("messageOverride.errorMessageTextProvided", errors.getFieldError("firstName").getCode());
	}
	
	@Test
	public void testSPELArgumentReplacementGivenRuleNameErrorCode() {
		Customer customer = new Customer();
		customer.setLastName("SPEL Argument Lookup Successful");
		Errors errors = doValidate("testSPELArgumentReplacementGivenRuleNameErrorCode.xml", customer).errors;
		assertEquals(1, errors.getErrorCount());
		assertEquals("(1) Test result: SPEL Argument Lookup Successful", 
			errors.getFieldError("firstName").getDefaultMessage());
		assertEquals("messageOverride.SPELArgs1.required", errors.getFieldError("firstName").getCode());
	}
	
	@Test
	public void testSPELArgumentReplacementGivenErrorMessageCode() {
		Customer customer = new Customer();
		customer.setLastName("SPEL Argument Lookup Successful");
		Errors errors = doValidate("testSPELArgumentReplacementGivenErrorMessageCode.xml", customer).errors;
		assertEquals(1, errors.getErrorCount());
		assertEquals("(2) Test result: SPEL Argument Lookup Successful", 
			errors.getFieldError("firstName").getDefaultMessage());
		assertEquals("messageOverride.SPELArgs2.required", errors.getFieldError("firstName").getCode());
	}
	
	@Test
	public void testSPELArgumentReplacementGivenErrorMessageText() {
		Customer customer = new Customer();
		customer.setLastName("SPEL Argument Lookup Successful");
		Errors errors = doValidate("testSPELArgumentReplacementGivenErrorMessageText.xml", customer).errors;
		assertEquals(1, errors.getErrorCount());
		assertEquals("(3) Test result: SPEL Argument Lookup Successful", 
			errors.getFieldError("firstName").getDefaultMessage());
		assertEquals("messageOverride.errorMessageTextProvided", errors.getFieldError("firstName").getCode());
	}
	
	@Test
	public void testSPELArgumentReplacementDoesntTriggerOnNumberedArgSubstitutedWithSPEL() {
		setCurrentFormPath("/foo/new");
		MockHttpServletRequest request = (MockHttpServletRequest) RequestUtils.getCurrentRequest();
		request.addParameter("blah", "${lastName}");
		
		Customer customer = new Customer();
		customer.setLastName("Oops, exploited");
		
		Errors errors = doValidate("testSPELArgumentReplacementDoesntTriggerOnNumberedArgSubstitutedWithSPEL.xml", customer).errors;
		assertEquals(1, errors.getErrorCount());
		assertEquals("Rule arg evaluated as: lastName", errors.getFieldError("firstName").getDefaultMessage());
		assertEquals("messageOverride.SPELInjectionArgs.matches", errors.getFieldError("firstName").getCode());
	}
	
	@Test
	public void testSPELArgumentReplacementDoesntTriggerOnMessageSPELSubstitutedWithSPEL() {
		
		Customer customer = new Customer();
		customer.setFirstName("${lastName}");
		customer.setLastName("Oops, exploited");
		
		Errors errors = doValidate("testSPELArgumentReplacementDoesntTriggerOnMessageSPELSubstitutedWithSPEL.xml", customer).errors;
		assertEquals(1, errors.getErrorCount());
		assertEquals("Evaluated SPEL as: lastName", errors.getFieldError("firstName").getDefaultMessage());
		assertEquals("messageOverride.SPELInjectionSPEL.matches", errors.getFieldError("firstName").getCode());
	}

}
