package org.springjutsu.validation.namespace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Properties;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class NamespaceVersionTest {

	private static final String XSD_LOC = "/org/springjutsu/validation/namespace/";
	private static final String SPRING_SCHEMA_LOC = "/META-INF/spring.schemas";
	
	@Test
	public void testSpringSchemasMatchesXsdVersion() throws IOException {
		Resource springDotSchemas = new ClassPathResource(SPRING_SCHEMA_LOC);
		Properties springDotSchemasProps = new Properties();
		springDotSchemasProps.load(springDotSchemas.getInputStream());
		assertEquals(1, springDotSchemasProps.size());
		String key = springDotSchemasProps.stringPropertyNames().iterator().next();
		String xsdFileName = key.substring(key.lastIndexOf("/") + 1);
		assertTrue(springDotSchemasProps.getProperty(key).endsWith("/" + xsdFileName));
		
		Resource xsdFile = new ClassPathResource(XSD_LOC + xsdFileName);
		assertTrue(xsdFile.exists());
	}
	
	private String cleanVersionNumber(String version) {
		return version
				.replaceAll("\\.[a-zA-Z]$", "") // remove minor fix release
				.replaceAll("-RELEASE$", ""); // remove snapshot indicator
	}

}
