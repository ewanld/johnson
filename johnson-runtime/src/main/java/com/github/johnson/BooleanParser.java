package com.github.johnson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class BooleanParser extends JohnsonParser<Boolean> {

	public BooleanParser(boolean nullable) {
		super(nullable);
	}
	
	@Override
	public Boolean doParse(JsonParser jp) throws JsonParseException, IOException {
		if (jp.getCurrentToken() == JsonToken.VALUE_NULL) {
			if (nullable) return null;
			else throw new JsonParseException(jp, "null not allowed!");
		}
		final boolean res = jp.getBooleanValue();
		return res;
	}

	public void serialize(Boolean value, JsonGenerator generator) throws IOException {
		generator.writeBoolean(value);
	}
}
