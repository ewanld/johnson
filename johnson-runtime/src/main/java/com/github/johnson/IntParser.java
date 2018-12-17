package com.github.johnson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class IntParser extends JohnsonParser<Integer> {
	public static final IntParser INSTANCE = new IntParser(false);
	public static final IntParser INSTANCE_NULLABLE = new IntParser(true);

	public IntParser(boolean nullable) {
		super(nullable);
	}

	@Override
	public Integer doParse(JsonParser jp) throws JsonParseException, IOException {
		if (jp.getCurrentToken() == JsonToken.VALUE_NULL) {
			if (nullable)
				return null;
			else throw new JsonParseException(jp, "null not allowed!");
		}
		final int res = jp.getIntValue();
		return res;
	}

	@Override
	public void doSerialize(Integer value, JsonGenerator generator) throws IOException {
		generator.writeNumber(value);
	}

}
