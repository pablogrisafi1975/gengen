package org.pgrisafi.gengen.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.pgrisafi.gengen.validation.EmptyValidator;
import org.pgrisafi.gengen.validation.JavaClassValidator;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface GenGenAnnotation {
	public Class<? extends JavaClassValidator> javaClassValidator() default EmptyValidator.class;
}
