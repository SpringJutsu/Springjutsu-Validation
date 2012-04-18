package org.springjutsu.validation.namespace;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springjutsu.validation.rules.ValidationRulesContainer;
import org.springjutsu.validation.test.entities.Customer;

@ContextConfiguration(
	"classpath:/org/springjutsu/validation/namespace/validationEntityDefinitionParserTest-config.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class ValidationEntityDefinitionParserTest {

	@Autowired
	private ValidationRulesContainer rulesContainer;
	
	/**
	 * Test for Bug #9
	 */
	@Test
	public void testParseUninstantiable() {
		assertFalse(rulesContainer.getModelRules(Customer.class).isEmpty());
	}
	
}
