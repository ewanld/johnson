package com.github.johnson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * @param <T> Type of array elements to be parsed
 * @param <CP> Type of the child parser
 */
public class ArrayParser<T, CP extends JohnsonParser<T>> extends JohnsonParser<List<T>> {
	public final CP childParser;
	private List<T> result = null;
	private Consumer<T> callback = (T t) -> result.add(t);

	public ArrayParser(boolean nullable, CP childParser) {
		super(nullable);
		this.childParser = childParser;
	}
	
	@Override
	protected List<T> doParse(JsonParser jp) throws JsonParseException, IOException {
		result = new ArrayList<T>();
		while (jp.nextToken() != JsonToken.END_ARRAY) {
			final T entity = childParser.doParse(jp);
			callback.accept(entity);
		}
		return Collections.unmodifiableList(result);
	}
	
	public Consumer<T> getCallback() {
		return callback;
	}

	public void setCallback(Consumer<T> callback) {
		assert callback != null;
		this.callback = callback;
	}

	@Override
	public void serialize(List<T> value, JsonGenerator generator) throws IOException {
		generator.writeStartArray();
		for (final T t : value) {
			childParser.serialize(t, generator);
		}
		generator.writeEndArray();
	}

	public List<T> getResult() {
		return result;
	}
}