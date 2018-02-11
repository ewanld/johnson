package com.github.johnson;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import com.github.visitorj.util.CompositeIterable;

/**
 * This goal will say a message.
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class CodeGeneratorMojo extends AbstractMojo {
	@Parameter(property = "schema", defaultValue = "${project.basedir}/src/main/resources/johnson-schema.json")
	private String schema;

	@Parameter(defaultValue = "${project.build.directory}/generated-sources/java", required = true)
	private String outputDirectory;

	@Parameter(property = "packageName", defaultValue = "")
	private String packageName;

	@Parameter(property = "addditionalSchemas")
	private List<String> additionalSchemas;

	@Parameter(property = "dtoClassNameSuffix", defaultValue = "")
	private String dtoClassNameSuffix;

	@Parameter(property = "dtoFieldsFinal", defaultValue = "false")
	private boolean dtoFieldsFinal;

	@Parameter(property = "dtoGenerateEmptyConstructor", defaultValue = "false")
	private boolean generateDtoEmptyConstructor;

	@Parameter(property = "dtoGenerateVisitor", defaultValue = "false")
	private boolean generateDtoVisitor;

	@Parameter(property = "dtoVisitorName", defaultValue = "JsonDto")
	private String dtoVisitorName;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Generating sources for schema " + schema);
		getLog().info(outputDirectory);
		if (packageName == null) packageName = "";
		final JsonFactory jackson = new JsonFactory();
		final Map<String, JohnsonType> namedTypes = new TreeMap<>();
		final Iterable<String> allSchemas = new CompositeIterable<>(Collections.singleton(schema), additionalSchemas);
		for (final String s : allSchemas) {
			try (final SpecParser specParser = new SpecParser(
					jackson.createParser(new BufferedReader(new FileReader(s))))) {
				namedTypes.putAll(specParser.read());
			} catch (final Exception e) {
				throw new MojoFailureException(e.getMessage(), e);
			}
		}
		final CodeGenerator codeGenerator = new CodeGenerator(outputDirectory, packageName, namedTypes);
		codeGenerator.setDtoClassNameSuffix(dtoClassNameSuffix);
		codeGenerator.setDtoFieldsFinal(dtoFieldsFinal);
		codeGenerator.setDtoVisitorName(dtoVisitorName);
		codeGenerator.setGenerateDtoEmptyConstructor(generateDtoEmptyConstructor);
		codeGenerator.setGenerateDtoVisitor(generateDtoVisitor);

		try {
			codeGenerator.gen();
		} catch (final IOException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}
}