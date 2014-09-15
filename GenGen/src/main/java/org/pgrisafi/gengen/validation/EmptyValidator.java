package org.pgrisafi.gengen.validation;

import org.pgrisafi.gengen.generator.Logger;

import com.thoughtworks.qdox.model.JavaClass;

public class EmptyValidator implements JavaClassValidator {

	@Override
	public void validate(JavaClass javaClass, Logger logger) throws ValidationException {
	}

}
