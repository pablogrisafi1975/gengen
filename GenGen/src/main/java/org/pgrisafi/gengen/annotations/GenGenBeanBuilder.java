package org.pgrisafi.gengen.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface GenGenBeanBuilder {
	String builderPackage() default "";

	String builderClassPrefix() default "";

	String builderClassSuffix() default "Builder";

	String builderSetterPrefix() default "";

}
