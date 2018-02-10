package com.github.johnson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public abstract class JohnsonParser<T> {
	protected final boolean nullable;

	public JohnsonParser(boolean nullable) {
		this.nullable = nullable;
	}

	public final T parse(JsonParser jp) throws JsonParseException, IOException {
		// advance to first token if needed
		if (jp.getCurrentToken() == null) jp.nextToken();

		// handle null case
		if (jp.getCurrentToken() == JsonToken.VALUE_NULL) {
			if (nullable)
				return null;
			else throw new JsonParseException(jp,
					"null not allowed for location: " + jp.getParsingContext().toString());
		}

		return doParse(jp);
	}

	protected abstract T doParse(JsonParser jp) throws JsonParseException, IOException;

	public abstract void serialize(T value, JsonGenerator generator) throws IOException;

}