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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.NullLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.pgrisafi.gengen.annotations.GenGenBeanBuilder;
import org.sonatype.plexus.build.incremental.BuildContext;

import com.google.common.collect.Maps;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.BeanProperty;
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
		for (JavaClass jc : projectBuilder.getClasses()) {
			boolean hasDelta = true;
			logger.info("Analizing class " + jc.getFullyQualifiedName());
			URL sourceURL = jc.getSource().getURL();
			try {
				File sourceFile = new File(sourceURL.toURI());
				if (!buildContext.hasDelta(sourceFile)) {
					hasDelta = false;
				}
			} catch (URISyntaxException ex) {
				logger.error("Class " + jc.getFullyQualifiedName() + " has a wrong url: " + jc.getSource().getURL(), ex);
			}
			if (hasDelta) {
				generateBuilderFor(jc, outputFolder);
			}
		}
	}

	public void generateBuilderFor(JavaClass jc, File outputFolder) {
		logger.info("Generating builder for class " + jc.getFullyQualifiedName());
		JavaAnnotation builderAnnotation = findAnnotation(jc.getAnnotations(), GenGenBeanBuilder.class);
		if (builderAnnotation != null) {
			validateProperClass(jc);
			String templateName = GenGenBeanBuilder.class.getSimpleName() + ".vm";
			Template template;
			try {
				template = Velocity.getTemplate(templateName);
			} catch (Exception e) {
				logger.error("Can not load template " + templateName, e);
				throw new RuntimeException(e);
			}

			VelocityContext context = new VelocityContext();

			Map<String, Object> params = Maps.newHashMap();
			for (Method method : GenGenBeanBuilder.class.getDeclaredMethods()) {
				String name = method.getName();
				if (builderAnnotation.getNamedParameterMap().containsKey(name)) {
					Object paramValue = builderAnnotation.getNamedParameter(name);
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
			context.put("clazz", jc);

			List<Field> fields = new LinkedList<Field>();
			for (BeanProperty bp : jc.getBeanProperties(true)) {
				if (!bp.getName().equals("class")) {
					Field param = new Field(bp.getType().getFullyQualifiedName(), bp.getName(),
							bp.getMutator() != null ? bp.getMutator().getName() : "");
					fields.add(param);
				}
			}
			context.put("fields", fields);

			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			Writer writer = new OutputStreamWriter(byteArrayOutputStream);
			try {
				template.merge(context, writer);
				writer.flush();
			} catch (IOException e) {
				logger.error("Can not generate for class: " + jc, e);
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

	private void validateProperClass(JavaClass jc) {
		if (jc.isInterface()) {
			logger.error("Can not use annotation GenGenBuilder in interface: " + jc.getFullyQualifiedName());
			throw new RuntimeException();
		}
		if (jc.isEnum()) {
			logger.error("Can not use annotation GenGenBuilder in enum: " + jc.getFullyQualifiedName());
			throw new RuntimeException();
		}
		if (jc.isAbstract()) {
			logger.error("Can not use annotation GenGenBuilder in abstract class: " + jc.getFullyQualifiedName());
			throw new RuntimeException();
		}
		if (jc.isInner()) {
			logger.error("Can not use annotation GenGenBuilder in inner class: " + jc.getFullyQualifiedName());
			throw new RuntimeException();
		}
	}

	private JavaAnnotation findAnnotation(List<JavaAnnotation> annotations, Class<?> clazz) {
		if (annotations != null) {
			for (JavaAnnotation annotation : annotations) {
				if (annotation.getType().getFullyQualifiedName().equals(clazz.getName())) {
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