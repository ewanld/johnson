package com.github.johnson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class DoubleParser extends JohnsonParser<Double> {
	public static final DoubleParser INSTANCE = new DoubleParser(false);
	public static final DoubleParser INSTANCE_NULLABLE = new DoubleParser(true);

	public DoubleParser(boolean nullable) {
		super(nullable);
	}

	@Override
	public Double doParse(JsonParser jp) throws JsonParseException, IOException {
		if (jp.getCurrentToken() == JsonToken.VALUE_NULL) {
			if (nullable)
				return null;
			else throw new JsonParseException(jp, "null not allowed!");
		}
		final double res = jp.getDoubleValue();
		return res;
	}

	@Override
	public void doSerialize(Double value, JsonGenerator generator) throws IOException {
		generator.writeNumber(value);
	}

}
