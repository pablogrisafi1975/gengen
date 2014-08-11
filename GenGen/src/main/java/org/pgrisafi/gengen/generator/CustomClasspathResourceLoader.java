package org.pgrisafi.gengen.generator;

import java.io.InputStream;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.pgrisafi.gengen.annotations.processor.AnnotationProcessor;

public class CustomClasspathResourceLoader extends ClasspathResourceLoader {
	private Logger logger;

	public CustomClasspathResourceLoader() {
		this.logger = new Logger() {

			@Override
			public void info(String message) {
				System.err.println(message);

			}

			@Override
			public void error(String message, Exception ex) {
				System.err.println(message);

			}

			@Override
			public void error(String message) {
				System.err.println(message);
			}
		};
	}

	public CustomClasspathResourceLoader(Logger logger) {
		this.logger = logger;
	}

	@Override
	public InputStream getResourceStream(String name) throws ResourceNotFoundException {
		InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(name);
		if (resourceAsStream == null) {
			logger.info("1 try null");
			resourceAsStream = this.getClass().getResourceAsStream(name);
			if (resourceAsStream == null) {
				logger.info("2 try null");
				resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
				if (resourceAsStream == null) {
					logger.info("3 try null");
					resourceAsStream = AnnotationProcessor.class.getResourceAsStream(name);
					if (resourceAsStream == null) {
						logger.info("4 try null");
						resourceAsStream = AnnotationProcessor.class.getClassLoader().getResourceAsStream(name);
					}
				}
			}
		}
		return resourceAsStream;
	}
}
