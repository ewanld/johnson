package com.github.johnson.codegen;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.github.johnson.codegen.types.JohnsonType;
import com.github.johnson.codegen.visitors.GenerateSample;

/**
 * Generate a sample JSON file matching a specification.
 */
public class SampleGenerator {
	private final JohnsonType type;

	public SampleGenerator(JohnsonType type) {
		this.type = type;
	}

	public void generate(JsonGenerator generator) throws IOException {
		new GenerateSample(generator).acceptAny(type);
		generator.flush();
	}
}
