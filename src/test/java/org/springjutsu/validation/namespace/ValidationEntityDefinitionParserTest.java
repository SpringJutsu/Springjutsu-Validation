package org.springjutsu.validation.namespace;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springjutsu.validation.rules.ValidationRulesContainer;

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
		assertFalse(rulesContainer.getModelRules(TestEntity.class).isEmpty());
	}
	
	public static class TestEntity {
		private String name;
		private TestEnum colorTest;
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public TestEnum getColorTest() {
			return colorTest;
		}
		public void setColorTest(TestEnum colorTest) {
			this.colorTest = colorTest;
		}
	}
	
	public static enum TestEnum {
		RED ("FF0000"),
		GREEN ("00FF00"),
		BLUE ("0000FF");
		
		private String color;
		
		TestEnum(String color) {
			this.color = color;
		}
		
		public String getColor() {
			return color;
		}
	}
	
}
