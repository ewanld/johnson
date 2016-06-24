package com.github.johnson.codegen;

import java.io.IOException;
import java.math.BigDecimal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.github.johnson.codegen.types.ArrayType;
import com.github.johnson.codegen.types.BooleanType;
import com.github.johnson.codegen.types.DecimalType;
import com.github.johnson.codegen.types.JohnsonType;
import com.github.johnson.codegen.types.LongType;
import com.github.johnson.codegen.types.MapType;
import com.github.johnson.codegen.types.ObjectType;
import com.github.johnson.codegen.types.RawType;
import com.github.johnson.codegen.types.RefType;
import com.github.johnson.codegen.types.StringType;

/**
 * Generate a sample JSON file matching a specification.
 */
public class SampleGenerator {
	private final JohnsonType type;

	public SampleGenerator(JohnsonType type) {
		this.type = type;
	}

	public void generate(JsonGenerator generator) throws IOException {
		new GenerateSample(generator).acceptAny(type);
		generator.flush();
	}

	private static class GenerateSample extends TypeVisitor {
		private final JsonGenerator generator;

		public GenerateSample(JsonGenerator generator) {
			this.generator = generator;
		}

		@FunctionalInterface
		private static interface CheckedRunnable<E extends Exception> {
			public void run() throws E;
		}

		private static <E extends Exception> void quiet(CheckedRunnable<E> runnable) {
			try {
				runnable.run();
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public boolean enterArray(ArrayType type) {
			quiet(() -> generator.writeStartArray());
			return true;
		}

		@Override
		public void exitArray(ArrayType type) {
			quiet(() -> generator.writeEndArray());
		}

		@Override
		public boolean enterMap(MapType type) {
			quiet(() -> generator.writeStartObject());
			return true;
		}

		@Override
		public void exitMap(MapType type) {
			quiet(() -> generator.writeEndObject());
		}

		@Override
		public boolean enterObject(ObjectType type) {
			quiet(() -> generator.writeStartObject());
			return true;
		}

		@Override
		public void exitObject(ObjectType type) {
			quiet(() -> generator.writeEndObject());
		}

		@Override
		public boolean enterObjectProp(ObjectProp objectProp) {
			quiet(() -> generator.writeFieldName(objectProp.getName()));
			return true;
		}

		@Override
		public void visitRef(RefType ref) {
			acceptAny(ref.getReferencedType());
		}

		@Override
		public void visitString(StringType type) {
			final String sample = "any string" + (type.isNullable() ? " | null" : "");
			quiet(() -> generator.writeString(sample));
		}

		@Override
		public void visitBool(BooleanType type) {
			quiet(() -> generator.writeBoolean(true));
		}

		@Override
		public void visitLong(LongType type) {
			quiet(() -> generator.writeNumber(4));
		}

		@Override
		public void visitDecimal(DecimalType type) {
			BigDecimal.valueOf(314, 3);
			quiet(() -> generator.writeNumber(BigDecimal.valueOf(314, 2)));
		}

		@Override
		public void visitRaw(RawType type) {
			quiet(() -> generator.writeString("any JSON"));
		}
	}
}
