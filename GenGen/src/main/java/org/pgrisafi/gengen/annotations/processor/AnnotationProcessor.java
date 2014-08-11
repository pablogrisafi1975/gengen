package org.pgrisafi.gengen.annotations.processor;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import org.pgrisafi.gengen.generator.Generator;
import org.pgrisafi.gengen.generator.Logger;

@SupportedAnnotationTypes({ "org.pgrisafi.gengen.annotations.GenGenBuilder" })
public class AnnotationProcessor extends AbstractProcessor {

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		showEnvs(roundEnv);
		File baseDir = findBaseDir();
		if (baseDir == null) {
			processingEnv.getMessager().printMessage(Kind.WARNING, "Can not find project basedir");
			return true;
		}
		String sourcesConfig = processingEnv.getOptions().get("sources");
		if (sourcesConfig == null || sourcesConfig.split(";", -1).length == 0) {
			processingEnv.getMessager().printMessage(Kind.WARNING, "No sources configuration found");
			return true;
		}
		Generator generator = new Generator();
		generator.init(new Logger() {

			@Override
			public void info(String message) {
				processingEnv.getMessager().printMessage(Kind.MANDATORY_WARNING, message);
				System.out.println(message);
			}

			@Override
			public void error(String message) {
				processingEnv.getMessager().printMessage(Kind.ERROR, message);
				System.err.println(message);
			}

			@Override
			public void error(String message, Exception ex) {
				processingEnv.getMessager().printMessage(Kind.ERROR, message);
				System.err.println(message);
				ex.printStackTrace();
			}
		});
		List<File> sourceFiles = new ArrayList<File>();

		File javaDir = new File(baseDir, "src/main/java");

		for (String source : sourcesConfig.split(";", -1)) {
			File sourceFile = new File(javaDir, source);
			sourceFiles.add(sourceFile);
		}
		generator.loadSources(sourceFiles);

		generator.generate(new File(baseDir, "target/generated-sources/gengen"));
		return true;
	}

	private void showEnvs(RoundEnvironment roundEnv) {
		StringBuilder sb = new StringBuilder("processingEnv.options: ");
		for (Map.Entry<String, String> option : processingEnv.getOptions().entrySet()) {
			sb.append(option.getKey());
			sb.append("=");
			sb.append(option.getValue());
			sb.append(", ");
		}
		processingEnv.getMessager().printMessage(Kind.WARNING, sb);

	}

	private File findBaseDir() {
		try {
			JavaFileObject javaFileObject = processingEnv.getFiler().createSourceFile("$$$$$.java");
			URI uri = javaFileObject.toUri();
			File file = new File(uri);
			if (file.exists()) {
				javaFileObject.delete();
			}
			// packinfo
			// --------------gengen ----generated-sources target ------basedir
			return file.getParentFile().getParentFile().getParentFile().getParentFile();
		} catch (Exception ex) {
			// throw new RuntimeException("Can not find project base directory",
			// ex);
			return null;
		}
	}
}
