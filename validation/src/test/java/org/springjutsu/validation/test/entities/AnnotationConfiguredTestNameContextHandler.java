package org.springjutsu.validation.test.entities;

import org.springjutsu.validation.context.ConfiguredContextHandler;
import org.springjutsu.validation.integrationTests.ValidationIntegrationTest.TestNameContextHandler;

@ConfiguredContextHandler(type="annotationConfiguredTestName")
public class AnnotationConfiguredTestNameContextHandler extends TestNameContextHandler {
	
}
