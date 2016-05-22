package org.springjutsu.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ValidationConfiguration.class)
public @interface EnableValidation {

	boolean addDefaultRuleExecutors() default true;
	boolean enableSuperclassFieldLabelLookup() default true;
	boolean addDefaultContextHandlers() default true;
	String errorMessagePrefix() default "errors";
	String fieldLabelPrefix() default "";
}
