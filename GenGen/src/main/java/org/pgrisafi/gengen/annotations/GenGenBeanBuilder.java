package org.pgrisafi.gengen.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.pgrisafi.gengen.validation.BeanBuilderValidator;

@GenGenAnnotation(javaClassValidator = BeanBuilderValidator.class)
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface GenGenBeanBuilder {
	String builderPackage() default "";

	String builderClassPrefix() default "";

	String builderClassSuffix() default "Builder";

	String builderSetterPrefix() default "";

}
