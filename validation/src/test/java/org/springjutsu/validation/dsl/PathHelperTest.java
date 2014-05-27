package org.springjutsu.validation.dsl;

import org.junit.Assert;
import org.junit.Test;
import org.springjutsu.validation.test.entities.Company;
import org.springjutsu.validation.test.entities.Customer;

public class PathHelperTest {

	@Test
	public void test()
	{
		Assert.assertEquals("address", PathHelper.forEntity(Customer.class).getAddress().toString());
		Assert.assertEquals("address.lineOne", PathHelper.forEntity(Customer.class).getAddress().getLineOne());
		Assert.assertEquals("customers.firstName", PathHelper.forEntity(Company.class).getCustomers().get(0).getFirstName());
		Assert.assertEquals("customers", PathHelper.forEntity(Company.class).getCustomers().toString());
	}

}
