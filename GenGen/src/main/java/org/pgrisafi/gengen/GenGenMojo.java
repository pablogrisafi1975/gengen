package org.pgrisafi.gengen;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.pgrisafi.gengen.generator.Generator;
import org.pgrisafi.gengen.generator.Logger;

/**
 * @goal generate-sources
 * @phase generate-sources
 */
public class GenGenMojo extends AbstractMojo {

	/**
	 * @parameter expression="${basedir}"
	 * @required
	 * @readonly
	 * @since 1.0
	 */
	private File baseDirectory;

	/**
	 * Sources
	 * 
	 * @parameter
	 * @required
	 */
	List<String> sources;

	/**
	 * @parameter default-value="target/generated-sources/gengen"
	 * @required
	 */
	File outputDirectory;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Generator generator = new Generator();
		generator.init(new Logger() {

			@Override
			public void info(String message) {
				getLog().debug(message);
			}

			@Override
			public void error(String message, Exception ex) {
				getLog().error(message, ex);

			}

			@Override
			public void error(String message) {
				getLog().error(message);
			}
		});

		List<File> sourceFiles = new ArrayList<File>();
		File javaDir = new File(baseDirectory, "src/main/java");
		for (String source : sources) {
			File sourceFile = new File(javaDir, source);
			sourceFiles.add(sourceFile);
		}
		generator.loadSources(sourceFiles);

		generator.generate(outputDirectory);

	}
}