package com.github.johnson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class LongParser extends JohnsonParser<Long> {

	public LongParser(boolean nullable) {
		super(nullable);
	}

	@Override
	public Long doParse(JsonParser jp) throws JsonParseException, IOException {
		if (jp.getCurrentToken() == JsonToken.VALUE_NULL) {
			if (nullable) return null;
			else throw new JsonParseException(jp, "null not allowed!");
		}
		final long res = jp.getLongValue();
		return res;
	}

	@Override
	public void serialize(Long value, JsonGenerator generator) throws IOException {
		generator.writeNumber(value);
	}

}
