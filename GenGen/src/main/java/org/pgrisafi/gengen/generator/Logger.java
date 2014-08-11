package org.pgrisafi.gengen.generator;

public interface Logger {
	void info(String message);

	void error(String message);

	void error(String string, Exception ex);

}
