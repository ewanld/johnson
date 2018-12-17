package com.github.johnson.codegen;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParseException;
import com.github.johnson.codegen.types.BooleanType;
import com.github.johnson.codegen.types.DecimalType;
import com.github.johnson.codegen.types.DoubleType;
import com.github.johnson.codegen.types.IntType;
import com.github.johnson.codegen.types.JohnsonType;
import com.github.johnson.codegen.types.LongType;
import com.github.johnson.codegen.types.RawType;
import com.github.johnson.codegen.types.RefType;
import com.github.johnson.codegen.types.StringType;
import com.github.johnson.codegen.types.ArrayType;
import com.github.johnson.codegen.types.ObjectType;
import com.github.toodle.ToodleReader;
import com.github.toodle.model.Definition;
import com.github.toodle.model.Type;

/**
 * Read a Johnson schema file (in format .2dl) and create the associated JohnsonType's.
 */
public class ToodleSchemaParser implements JohnsonSchemaParser {
	private final ToodleReader reader;

	public ToodleSchemaParser(ToodleReader reader) {
		this.reader = reader;
	}

	@Override
	public Map<String, JohnsonType> read() throws JsonParseException, IOException {
		final Collection<Definition> definitions = reader.read();
		final Map<String, JohnsonType> res = new HashMap<>();

		for (final Definition definition : definitions) {
			res.put(definition.getName(), readDefinition(definition.getType()));
		}
		return res;
	}

	private JohnsonType readDefinition(Type type) {
		final String typeName = type.getName();
		final boolean nullable = type.getAnnotation("nullable") != null;
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
		} else if (typeName.equals("array")) {
			return new ArrayType(readDefinition(type.getTypeParams().get(0)), nullable);
		} else if (typeName.equals("object")) {
			final JohnsonType baseType = type.getAnnotation("extends") == null ? null
					: new RefType(type.getAnnotation("extends").getStringParams().get(0), false);
			final List<ObjectProp> children = type.getChildren().stream().map(d -> new ObjectProp(d.getName(),
					readDefinition(d.getType()), !d.getModifiers().contains("optional"))).collect(Collectors.toList());
			return new ObjectType(nullable, children, baseType);
		} else {
			return new RefType(typeName, nullable);
		}

	}

	@Override
	public void close() throws IOException {
	}

}
