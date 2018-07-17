package com.github.johnson.codegen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.github.johnson.codegen.types.ArrayType;
import com.github.johnson.codegen.types.BooleanType;
import com.github.johnson.codegen.types.DecimalType;
import com.github.johnson.codegen.types.DoubleType;
import com.github.johnson.codegen.types.IntType;
import com.github.johnson.codegen.types.JohnsonType;
import com.github.johnson.codegen.types.LongType;
import com.github.johnson.codegen.types.ObjectType;
import com.github.johnson.codegen.types.RawType;
import com.github.johnson.codegen.types.RefType;
import com.github.johnson.codegen.types.StringType;
import com.github.johnson.codegen.visitors.FillRefs;

/**
 * Read a Johnson schema file (in format JSON) and create the associated JohnsonType's.
 */
public class JsonSchemaParser implements JohnsonSchemaParser {
	private final JsonParser jp;

	public JsonSchemaParser(JsonParser jp) {
		this.jp = jp;
	}

	@Override
	public void close() throws IOException {
		jp.close();
	}

	@Override
	public Map<String, JohnsonType> read() throws IOException {
		final Map<String, JohnsonType> res = new TreeMap<>();
		jp.nextToken();
		assert jp.getCurrentToken() == JsonToken.START_OBJECT;
		while (jp.nextToken() != JsonToken.END_OBJECT) {
			assert jp.getCurrentToken() == JsonToken.FIELD_NAME;
			final String name = jp.getCurrentName();
			final JohnsonType val = readJohnsonType();
			res.put(name, val);
		}

		new FillRefs(res).fillAllRefs();
		return res;
	}

	private ObjectType readObjectType() throws JsonParseException, IOException {
		final List<ObjectProp> props = new ArrayList<>();
		JohnsonType baseType = null;
		assert jp.getCurrentToken() == JsonToken.START_OBJECT;
		while (jp.nextToken() != JsonToken.END_OBJECT) {
			final ObjectProp objectProp = readObjectProp();

			// handle special case: "$extends" key
			final boolean extendsProp = objectProp.getName().equals("$extends");
			if (extendsProp) {
				baseType = objectProp.getType();
			} else {
				props.add(objectProp);
			}
		}

		return new ObjectType(false, props, baseType);
	}

	private ObjectProp readObjectProp() throws IOException {
		assert jp.getCurrentToken() == JsonToken.FIELD_NAME;
		final String name_spec = jp.getCurrentName();
		final boolean required = !name_spec.endsWith("?");
		final String propName = !required ? name_spec.substring(0, name_spec.length() - 1) : name_spec;
		final JohnsonType type = readJohnsonType();
		return new ObjectProp(propName, type, required);
	}

	private JohnsonType readJohnsonType() throws IOException {
		final JsonToken token = jp.nextToken();
		if (token == JsonToken.START_ARRAY) {
			final ArrayType arrayType = new ArrayType(readJohnsonType(), false);
			jp.nextToken();
			assert jp.getCurrentToken() == JsonToken.END_ARRAY;
			return arrayType;

		} else if (token == JsonToken.START_OBJECT) {
			final ObjectType objectType = readObjectType();
			return objectType;

		} else if (token == JsonToken.VALUE_STRING) {
			return readTypeSpec(jp.getValueAsString());
		} else {
			throw new JsonParseException(jp, "Unexpected tokken: " + token);
		}
	}

	private JohnsonType readTypeSpec(String spec) {
		final boolean nullable = spec.endsWith("?");
		final String typeName = nullable ? spec.substring(0, spec.length() - 1) : spec;

		if (typeName.equals("bool")) {
			return new BooleanType(nullable);
		} else if (typeName.equals("string")) {
			return new StringType(nullable);
		} else if (typeName.equals("int")) {
			return new IntType(nullable);
		} else if (typeName.equals("long")) {
			return new LongType(nullable);
		} else if (typeName.equals("decimal")) {
			return new DecimalType(nullable);
		} else if (typeName.equals("double")) {
			return new DoubleType(nullable);
		} else if (typeName.equals("raw")) {
			return new RawType(nullable);
		} else {
			return new RefType(typeName, nullable);
		}
	}
}