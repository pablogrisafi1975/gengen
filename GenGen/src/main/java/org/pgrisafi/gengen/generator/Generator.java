package org.pgrisafi.gengen.generator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.NullLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.pgrisafi.gengen.annotations.GenGenAnnotation;
import org.pgrisafi.gengen.validation.JavaClassValidator;
import org.sonatype.plexus.build.incremental.BuildContext;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaSource;

public class Generator {
	private JavaProjectBuilder projectBuilder;
	private Logger logger;
	private BuildContext buildContext;

	public void init(Logger logger) {
		projectBuilder = new JavaProjectBuilder();
		this.logger = logger;

		try {

			Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
			Velocity.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
			Velocity.setProperty("runtime.log.logsystem.class", NullLogChute.class.getName());
			Velocity.init();

		} catch (Exception ex) {
			logger.error("Error loading Velocity", ex);
			throw new RuntimeException(ex);
		}
	}

	public void loadSources(List<File> sources) {
		if (sources == null || sources.isEmpty()) {
			logger.error("No source file specified");
		}
		for (File source : sources) {
			try {
				if (source.isDirectory()) {
					logger.info("Including folder and subfolders:" + source.getAbsolutePath());
					projectBuilder.addSourceTree(source);
				} else {
					logger.info("Including file:" + source.getAbsolutePath());
					projectBuilder.addSource(source);
				}
			} catch (Exception ex) {
				logger.error("Can not load source: " + source.getAbsolutePath(), ex);
				throw new RuntimeException(ex);
			}
		}
	}

	public void generate(File outputFolder) {
		for (JavaClass javaClass : projectBuilder.getClasses()) {
			boolean hasDelta = true;
			logger.info("Analizing class " + javaClass.getFullyQualifiedName());
			URL sourceURL = javaClass.getSource().getURL();
			try {
				File sourceFile = new File(sourceURL.toURI());
				if (!buildContext.hasDelta(sourceFile)) {
					hasDelta = false;
				}
			} catch (URISyntaxException ex) {
				logger.error("Class " + javaClass.getFullyQualifiedName() + " has a wrong url: "
						+ javaClass.getSource().getURL(), ex);
			}
			if (hasDelta) {
				List<Class<?>> genGenAnnotations = findGenGenAnnotations(javaClass);
				for (Class<?> genGenAnnotation : genGenAnnotations) {
					generateCodeFor(javaClass, outputFolder, genGenAnnotation);
				}
			}
		}
	}

	private List<Class<?>> findGenGenAnnotations(JavaClass javaClass) {
		logger.info("Looking for annotations in class " + javaClass + " list:" + javaClass.getAnnotations());
		List<Class<?>> genGenAnnotations = Lists.newArrayList();

		for (JavaAnnotation javaAnnotation : javaClass.getAnnotations()) {
			Class<?> javaAnnotationClass = null;
			try {
				javaAnnotationClass = Class.forName(javaAnnotation.getType().getCanonicalName());
			} catch (ClassNotFoundException ex) {
				logger.error("Class " + javaClass.getFullyQualifiedName() + " has annotation : " + javaAnnotation
						+ " that can not be loaded", ex);
			}
			logger.info("Annotations in annotation class " + javaAnnotationClass + " list:"
					+ Arrays.toString(javaAnnotationClass.getAnnotations()));
			if (javaAnnotationClass.isAnnotationPresent(GenGenAnnotation.class)) {
				logger.info("Using " + javaAnnotationClass);
				genGenAnnotations.add(javaAnnotationClass);
			}
		}
		return genGenAnnotations;
	}

	public void generateCodeFor(JavaClass javaClass, File outputFolder, Class<?> gengenAnnotationClass) {
		logger.info("Generating code for class " + javaClass.getFullyQualifiedName() + " using annotation "
				+ gengenAnnotationClass.getCanonicalName());
		JavaAnnotation annotation = findGenGenAnnotation(javaClass.getAnnotations(), gengenAnnotationClass);
		if (annotation != null) {
			GenGenAnnotation genGenAnnotation = gengenAnnotationClass.getAnnotation(GenGenAnnotation.class);
			Class<? extends JavaClassValidator> javaClassValidatorClass = genGenAnnotation.javaClassValidator();
			JavaClassValidator javaClassValidator = null;
			try {
				javaClassValidator = javaClassValidatorClass.newInstance();
			} catch (Exception ex) {
				logger.error("Can not instanciate validator " + javaClassValidatorClass.getCanonicalName(), ex);
				throw new RuntimeException(ex);
			}

			javaClassValidator.validate(javaClass, logger);

			String templateName = gengenAnnotationClass.getSimpleName() + ".vm";
			Template template;
			try {
				template = Velocity.getTemplate(templateName);
			} catch (Exception e) {
				logger.error("Can not load template " + templateName, e);
				throw new RuntimeException(e);
			}

			VelocityContext context = new VelocityContext();

			Map<String, Object> params = Maps.newHashMap();
			for (Method method : gengenAnnotationClass.getDeclaredMethods()) {
				String name = method.getName();
				if (annotation.getNamedParameterMap().containsKey(name)) {
					Object paramValue = annotation.getNamedParameter(name);
					/*
					 * For some reason qdox gives you the String literal, not
					 * the string value That is to say, surrounded by quotes.
					 * You get >"with"< instead of >with< So if value is a
					 * String, remove quotes
					 */
					if (paramValue instanceof String) {
						String valueWithQuotes = (String) paramValue;
						params.put(name, valueWithQuotes.substring(1, valueWithQuotes.length() - 1));
					} else {
						params.put(name, paramValue);
					}
				} else {
					params.put(name, method.getDefaultValue());
				}
			}

			context.put("params", params);
			context.put("clazz", javaClass);
			context.put("stringUtils", new StringUtils());

			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			Writer writer = new OutputStreamWriter(byteArrayOutputStream);
			try {
				template.merge(context, writer);
				writer.flush();
			} catch (IOException e) {
				logger.error("Can not generate for class: " + javaClass, e);
			}
			byte[] inMemoryClassFile = byteArrayOutputStream.toByteArray();

			JavaSource javaSource = projectBuilder.addSource(new InputStreamReader(new ByteArrayInputStream(
					inMemoryClassFile)));

			File pd = new File(outputFolder, javaSource.getPackageName().replaceAll("\\.", "/"));
			if (!pd.exists()) {
				pd.mkdirs();
			}

			File javaFile = new File(pd, getPublicClassName(javaSource) + ".java");
			logger.info("Writing file " + javaFile.getAbsolutePath());
			OutputStream outputStream = null;
			try {
				outputStream = buildContext.newFileOutputStream(javaFile);
				IOUtils.copy(new ByteArrayInputStream(inMemoryClassFile), outputStream);
				outputStream.flush();
			} catch (Exception ex) {
				logger.error("Can not write file: " + javaFile.getAbsolutePath(), ex);
			} finally {
				IOUtils.closeQuietly(outputStream);
			}

		}
	}

	private String getPublicClassName(JavaSource javaSource) {
		for (JavaClass javaClass : javaSource.getClasses()) {
			if (javaClass.isPublic()) {
				return javaClass.getName();
			}
		}
		return null;
	}

	private JavaAnnotation findGenGenAnnotation(List<JavaAnnotation> allAnnotations, Class<?> annotationClass) {
		if (allAnnotations != null) {
			for (JavaAnnotation annotation : allAnnotations) {
				if (annotation.getType().getFullyQualifiedName().equals(annotationClass.getName())) {
					return annotation;
				}
			}
		}
		return null;

	}

	public void setBuildContext(BuildContext buildContext) {
		this.buildContext = buildContext;
	}

}