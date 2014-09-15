package org.pgrisafi.gengen.validation;

import org.pgrisafi.gengen.generator.Logger;

import com.thoughtworks.qdox.model.JavaClass;

public interface JavaClassValidator {
	public void validate(JavaClass javaClass, Logger logger) throws ValidationException;
}
