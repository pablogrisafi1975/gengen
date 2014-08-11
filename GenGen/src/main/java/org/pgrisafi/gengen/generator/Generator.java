package org.pgrisafi.gengen.generator;

import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.pgrisafi.gengen.annotations.GenGenBuilder;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.Annotation;
import com.thoughtworks.qdox.model.BeanProperty;
import com.thoughtworks.qdox.model.JavaClass;

public class Generator {
	private JavaDocBuilder docBuilder;
	private Logger logger;

	public void init(Logger logger) {
		docBuilder = new JavaDocBuilder();
		this.logger = logger;

		try {

			Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
			Velocity.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
			// Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER,
			// "customclasspath");
			// Velocity.setProperty("customclasspath.resource.loader.class",
			// CustomClasspathResourceLoader.class.getName());

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
					docBuilder.addSourceTree(source);
				} else {
					logger.info("Including file:" + source.getAbsolutePath());
					docBuilder.addSource(source);
				}
			} catch (Exception ex) {
				logger.error("Can not load source: " + source.getAbsolutePath(), ex);
				throw new RuntimeException(ex);
			}
		}
	}

	public void generate(File outputFolder) {
		for (JavaClass jc : docBuilder.getClasses()) {
			generateBuilderFor(jc, outputFolder);
		}
	}

	public void generateBuilderFor(JavaClass jc, File outputFolder) {
		Annotation builderAnnotation = findAnnotation(jc.getAnnotations(), GenGenBuilder.class);
		if (builderAnnotation != null) {
			validateProperClass(jc);
			String templateName = "GenGenBuilder.vm";
			Template template;
			try {
				// template = Velocity.getTemplate("GenGenBuilder.vm");
				template = new Template();
				template.setResourceLoader(new CustomClasspathResourceLoader(logger));
				template.setName(templateName);
				template.setRuntimeServices(RuntimeSingleton.getRuntimeServices());
				template.process();

			} catch (Exception e) {
				logger.error("Can not load template " + templateName, e);
				throw new RuntimeException(e);
			}

			String packageName = jc.getPackageName();
			String builderName = jc.getName() + "Builder";

			VelocityContext context = new VelocityContext();
			context.put("packageName", packageName);
			context.put("builderName", builderName);
			context.put("resultClass", jc.asType().toString());

			List<Field> fields = new LinkedList<Field>();
			for (BeanProperty bp : jc.getBeanProperties(true)) {
				if (!bp.getName().equals("class")) {
					Field param = new Field(bp.getType().toGenericString(), bp.getName(), bp.getMutator() != null ? bp
							.getMutator().getName() : "");
					fields.add(param);
				}
			}
			context.put("fields", fields);

			File pd = new File(outputFolder, packageName.replaceAll("\\.", "/"));
			pd.mkdirs();

			File javaFile = null;

			try {

				javaFile = new File(pd, builderName + ".java");
				FileWriter out = new FileWriter(javaFile);
				try {
					template.merge(context, out);
				} finally {
					out.flush();
					out.close();
				}
			} catch (Exception ex) {
				logger.error("Can not write file: " + javaFile.getAbsolutePath(), ex);
			}

		}

		// for (JavaMethod m : jc.getMethods()) {
		// if (m.isConstructor()) {
		// if (builderAnnotation != null) {
		// Boolean builderAbstract = (Boolean)
		// builderAnnotation.getNamedParameter("abstract");
		// if (builderAbstract == null) {
		// builderAbstract = false;
		// }
		//
		// String builderName = (String)
		// builderAnnotation.getNamedParameter("name");
		// if (builderName == null) {
		// if (builderAbstract)
		// builderName = "Abstract" + builderName;
		// else
		// builderName = builderName;
		// }
		//
		// String createMethod = (String)
		// builderAnnotation.getNamedParameter("createMethod");
		// if (createMethod == null)
		// createMethod = "create";
		//
		// String packageName = (String)
		// builderAnnotation.getNamedParameter("package");
		// if (packageName == null)
		// packageName = packageName;
		//
		// String extendsClass = (String)
		// builderAnnotation.getNamedParameter("extends");
		//
		// VelocityContext context = new VelocityContext();
		// context.put("packageName", packageName);
		// context.put("builderName", builderName);
		// context.put("resultClass", jc.asType().toString());
		// context.put("createMethod", createMethod);
		// context.put("extendsClass", extendsClass);
		//
		// List<Field> ps = new LinkedList<Field>();
		// List<Field> cs = new LinkedList<Field>();
		// for (JavaParameter p : m.getParameters()) {
		// Field param = new Field(p.getType().toGenericString(), p.getName());
		// if (!p.getName().startsWith("_"))
		// ps.add(param);
		// cs.add(param);
		// }
		// context.put("parameters", ps);
		// context.put("arguments", cs);
		//
		// File pd = new File(outputFolder, packageName.replaceAll("\\.", "/"));
		// pd.mkdirs();
		//
		// File javaFile = null;
		//
		// try {
		//
		// javaFile = new File(pd, builderName + ".java");
		// FileWriter out = new FileWriter(javaFile);
		// try {
		// template.merge(context, out);
		// } finally {
		// out.flush();
		// out.close();
		// }
		// } catch (Exception ex) {
		// logger.error("Can not write file: " + javaFile.getAbsolutePath(),
		// ex);
		// }
		//
		// // if (getLog().isDebugEnabled()) {
		// // getLog().debug(builderName + ".java :");
		// // // TODO: show
		// // // getLog().debug(st.toString());
		// // }
		// }
		// }
		// }
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

	private Annotation findAnnotation(Annotation[] annotations, Class<?> clazz) {
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				if (annotation.getType().getFullyQualifiedName().equals(clazz.getName())) {
					return annotation;
				}
			}
		}
		return null;

	}

}