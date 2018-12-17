package com.github.johnson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;

public abstract class JohnsonParser<T> {
	protected final boolean nullable;

	public JohnsonParser(boolean nullable) {
		this.nullable = nullable;
	}

	protected abstract T doParse(JsonParser jp) throws JsonParseException, IOException;

	public abstract void doSerialize(T value, JsonGenerator generator) throws IOException;

	public final T parse(JsonParser jp) throws JsonParseException, IOException {
		// advance to first token if needed
		if (jp.getCurrentToken() == null) jp.nextToken();

		// handle null case
		if (jp.getCurrentToken() == JsonToken.VALUE_NULL) {
			if (nullable)
				return null;
			else throw new JsonParseException(jp,
					"null not allowed for location: " + contextToString(jp.getParsingContext()));
		}

		return doParse(jp);
	}

	public final void serialize(T value, JsonGenerator generator) throws IOException {
		if (value == null) {
			if (nullable) {
				generator.writeNull();
			} else {
				throw new JsonGenerationException(
						"null not allowed for location: " + contextToString(generator.getOutputContext()), generator);
			}
		} else {
			doSerialize(value, generator);
		}
	}

	private String contextToString(JsonStreamContext outputContext) {
		if (outputContext == null) return "";
		if (outputContext.getParent() != null)
			return contextToString(outputContext.getParent()) + "/" + outputContext.toString();
		return outputContext.toString();
	}

}