package org.pgrisafi.gengen;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
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
	 * @parameter default-value="target/generated-sources/gengen"
	 * @required
	 */
	File outputDirectory;

	/** @component */
	private BuildContext buildContext;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
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
		logger.info("incremental:_" + buildContext.isIncremental());
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

	}

	private void logToFile(String message) {
		try {
			org.apache.commons.io.FileUtils.writeStringToFile(new File("c:/temp/gengen.log"), message + "\r\n", true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}