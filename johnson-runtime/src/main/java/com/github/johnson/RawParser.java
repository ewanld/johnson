package com.github.johnson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RawParser extends JohnsonParser<JsonNode> {
	public static final RawParser INSTANCE = new RawParser(false);
	public static final RawParser INSTANCE_NULLABLE = new RawParser(true);

	private final ObjectMapper mapper = new ObjectMapper();

	public RawParser(boolean nullable) {
		super(nullable);
	}

	@Override
	protected JsonNode doParse(JsonParser jp) throws JsonParseException, IOException {
		final JsonNode res = mapper.readTree(jp);
		return res;
	}

	@Override
	public void doSerialize(JsonNode value, JsonGenerator generator) throws IOException {
		mapper.writeTree(generator, value);
	}

}
