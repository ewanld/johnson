package com.github.johnson;

import java.io.IOException;
import java.math.BigDecimal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class DecimalParser extends JohnsonParser<BigDecimal> {

	public DecimalParser(boolean nullable) {
		super(nullable);
	}
	
	@Override
	public BigDecimal doParse(JsonParser jp) throws JsonParseException, IOException {
		if (jp.getCurrentToken() == JsonToken.VALUE_NULL) {
			if (nullable) return null;
			else throw new JsonParseException(jp, "null not allowed!");
		}
		final BigDecimal res = jp.getDecimalValue();
		return res;
	}

	@Override
	public void serialize(BigDecimal value, JsonGenerator generator) throws IOException {
		generator.writeNumber(value);
	}

}
