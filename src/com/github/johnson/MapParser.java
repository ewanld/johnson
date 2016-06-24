package com.github.johnson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class MapParser<V, CP extends JohnsonParser<V>> extends JohnsonParser<Map<String, V>> {
	public final CP childParser;
	private final Map<String, V> result = new HashMap<String, V>();
	private BiConsumer<String, V> callback = (key, value) -> result.put(key, value);

	public MapParser(boolean nullable, CP childParser) {
		super(nullable);
		this.childParser = childParser;
	}

	@Override
	protected Map<String, V> doParse(JsonParser jp) throws JsonParseException, IOException {
		assert jp.getCurrentToken() == JsonToken.START_OBJECT;

		while (jp.nextToken() != JsonToken.END_OBJECT) {
			assert jp.getCurrentToken() == JsonToken.FIELD_NAME;
			final String key = jp.getCurrentName();
			jp.nextToken();
			final V value = childParser.parse(jp);
			callback.accept(key, value);
		}
		return result;
	}

	public BiConsumer<String, V> getCallback() {
		return callback;
	}

	public void setCallback(BiConsumer<String, V> callback) {
		this.callback = callback;
	}

	@Override
	public void serialize(Map<String, V> value, JsonGenerator generator) throws IOException {
		generator.writeStartObject();
		for (final Entry<String, V> e : value.entrySet()) {
			generator.writeFieldName(e.getKey());
			childParser.serialize(e.getValue(), generator);
		}
		generator.writeEndObject();
	}

	public Map<String, V> getResult() {
		return result;
	}
}
