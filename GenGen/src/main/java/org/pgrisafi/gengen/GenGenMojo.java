package org.pgrisafi.gengen;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.pgrisafi.gengen.generator.Generator;
import org.pgrisafi.gengen.generator.Logger;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * @goal generate-sources
 * @phase generate-sources
 */
public class GenGenMojo extends AbstractMojo {

	/**
	 * @parameter property="basedir"
	 * @required
	 * @readonly
	 * @since 1.0
	 */
	private File basedir;

	/**
	 * @parameter
	 */
	String logFileName;

	/**
	 * @parameter default-value="target/generated-sources/gengen"
	 * @readonly
	 * @required
	 */
	private File outputDirectory;

	/**
	 * @component
	 */
	private BuildContext buildContext;

	/**
	 * @parameter property="project"
	 * @required
	 * @readonly
	 * @since 1.0
	 */
	private MavenProject project;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		logToFile("logFileName:" + logFileName);
		Generator generator = new Generator();
		generator.setBuildContext(buildContext);
		Logger logger = new Logger() {

			@Override
			public void info(String message) {
				logToFile("INFO:" + message);
				getLog().debug(message);
			}

			@Override
			public void error(String message, Exception ex) {
				logToFile("ERROR:" + message + ExceptionUtils.getFullStackTrace(ex));
				getLog().error(message, ex);

			}

			@Override
			public void error(String message) {
				logToFile("ERROR:" + message);
				getLog().error(message);
			}
		};
		logger.info("incremental:" + buildContext.isIncremental());
		generator.init(logger);

		File javaDir = new File(basedir, "src/main/java");

		generator.loadSources(Arrays.asList(javaDir));
		org.codehaus.plexus.util.Scanner scanner = buildContext.newScanner(javaDir);
		// code below is standard plexus Scanner stuff
		scanner.setIncludes(new String[] { "**/*.java" });
		scanner.scan();
		String[] includedFiles = scanner.getIncludedFiles();
		logger.info("Changed files: " + Arrays.toString(includedFiles));
		generator.generate(outputDirectory);

		project.addCompileSourceRoot(outputDirectory.getAbsolutePath());

	}

	private void logToFile(String message) {
		try {
			if (StringUtils.isNotBlank(logFileName)) {
				FileUtils.writeStringToFile(new File(logFileName), message + "\r\n", true);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}