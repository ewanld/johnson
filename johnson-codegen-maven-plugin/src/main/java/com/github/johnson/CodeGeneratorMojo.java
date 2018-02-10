package com.github.johnson;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.fasterxml.jackson.core.JsonFactory;
import com.github.johnson.codegen.CodeGenerator;
import com.github.johnson.codegen.SpecParser;
import com.github.johnson.codegen.types.JohnsonType;

/**
 * This goal will say a message.
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class CodeGeneratorMojo extends AbstractMojo {
	@Parameter(property = "schema", defaultValue = "${project.basedir}/src/main/resources/johnson-schema.json")
	private String schema;

	@Parameter(defaultValue = "${project.build.directory}/generated-sources/java", required = true)
	private String outputDirectory;

	@Parameter(property = "dtoClassNameSuffix", defaultValue = "")
	private String dtoClassNameSuffix;

	@Parameter(property = "dtoFieldsFinal", defaultValue = "false")
	private boolean dtoFieldsFinal;

	@Parameter(property = "dtoVisitorName", defaultValue = "JsonDto")
	private String dtoVisitorName;

	@Parameter(property = "generateDtoEmptyConstructor", defaultValue = "false")
	private boolean generateDtoEmptyConstructor;

	@Parameter(property = "generateDtoVisitor", defaultValue = "false")
	private boolean generateDtoVisitor;

	@Parameter(property = "packageName", defaultValue = "")
	private String packageName;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Generating sources for schema " + schema);
		getLog().info(outputDirectory);
		if (packageName == null) packageName = "";
		final JsonFactory jackson = new JsonFactory();
		try (final SpecParser specParser = new SpecParser(
				jackson.createParser(new BufferedReader(new FileReader(schema))))) {
			final Map<String, JohnsonType> namedTypes = specParser.read();
			final CodeGenerator codeGenerator = new CodeGenerator(outputDirectory, packageName, namedTypes);
			codeGenerator.setDtoClassNameSuffix(dtoClassNameSuffix);
			codeGenerator.setDtoFieldsFinal(dtoFieldsFinal);
			codeGenerator.setDtoVisitorName(dtoVisitorName);
			codeGenerator.setGenerateDtoEmptyConstructor(generateDtoEmptyConstructor);
			codeGenerator.setGenerateDtoVisitor(generateDtoVisitor);

			codeGenerator.gen();
		} catch (final Exception e) {
			throw new MojoFailureException(e.getMessage(), e);
		}
	}
}