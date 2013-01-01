package org.springjutsu.validation.integrationTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.Errors;
import org.springjutsu.validation.test.entities.Company;
import org.springjutsu.validation.test.entities.Customer;

public class CollectionIntegrationTest extends ValidationIntegrationTest {
	
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
		assertEquals("customer.firstName", ((DefaultMessageSourceResolvable) errors.getFieldError("customers[1].firstName").getArguments()[0]).getCode());
		assertEquals("errors.required", errors.getFieldError("customers[2].firstName").getCode());
		assertEquals("customer.firstName", ((DefaultMessageSourceResolvable) errors.getFieldError("customers[2].firstName").getArguments()[0]).getCode());
	}
	
	@Test
	public void testLocalCollectionRules() {
		Company company = new Company();
		company.setName("Awesome Co.");
		company.getSlogans().add("Doing right.");
		company.getSlogans().add("Doing our customers right.");
		company.getSlogans().add("Doing our customers right in a non-suggestive way.");
		
		Errors errors = doValidate("testCollectionRules.xml", company).errors;
		assertEquals(2, errors.getErrorCount());
		assertEquals("errors.maxLength", errors.getFieldError("slogans[1]").getCode());
		assertEquals("company.slogans", ((DefaultMessageSourceResolvable) errors.getFieldError("slogans[1]").getArguments()[0]).getCode());
		assertEquals("errors.maxLength", errors.getFieldError("slogans[2]").getCode());
		assertEquals("company.slogans", ((DefaultMessageSourceResolvable) errors.getFieldError("slogans[2]").getArguments()[0]).getCode());
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
