package org.pgrisafi.gengen.validation;

import org.pgrisafi.gengen.generator.Logger;

import com.thoughtworks.qdox.model.JavaClass;

public class BeanBuilderValidator implements JavaClassValidator {

	@Override
	public void validate(JavaClass javaClass, Logger logger) throws ValidationException {
		if (javaClass.isInterface()) {
			throw new ValidationException("Can not use annotation GenGenBeanBuilder in interface: "
					+ javaClass.getFullyQualifiedName());
		}
		if (javaClass.isEnum()) {
			throw new ValidationException("Can not use annotation GenGenBeanBuilder in enum: "
					+ javaClass.getFullyQualifiedName());
		}
		if (javaClass.isAbstract()) {
			throw new ValidationException("Can not use annotation GenGenBeanBuilder in abstract class: "
					+ javaClass.getFullyQualifiedName());
		}
		if (javaClass.isInner()) {
			throw new ValidationException("Can not use annotation GenGenBeanBuilder in inner class: "
					+ javaClass.getFullyQualifiedName());
		}
	}

}
