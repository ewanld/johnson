package com.github.johnson.codegen;

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
 * Factory for instances of JohnsonType and its subtypes.
 */
public class JohnsonTypeFactory {
	private JohnsonTypeFactory() {
		// prevent instanciation
	}

	public static StringType str() {
		return new StringType();
	}

	public static StringType str(boolean nullable) {
		return new StringType(nullable);
	}

	public static LongType long_() {
		return new LongType();
	}

	public static LongType long_(boolean nullable) {
		return new LongType(nullable);
	}

	public static BooleanType bool() {
		return new BooleanType();
	}

	public static BooleanType bool(boolean nullable) {
		return new BooleanType(nullable);
	}

	public static DecimalType decimal() {
		return new DecimalType();
	}

	public static DecimalType decimal(boolean nullable) {
		return new DecimalType(nullable);
	}

	public static ObjectType obj(ObjectProp... properties) {
		return new ObjectType(properties);
	}

	public static ObjectProp prop(String name, JohnsonType type) {
		return new ObjectProp(name, type, true);
	}

	public static ObjectProp prop(String name, boolean required, JohnsonType type) {
		return new ObjectProp(name, type, required);
	}

	public static ArrayType array(JohnsonType type) {
		return new ArrayType(type, false);
	}

	public static RefType ref(String typeName) {
		return new RefType(typeName, false);
	}

	public static RefType ref(String typeName, boolean nullable) {
		return new RefType(typeName, nullable);
	}

	public static MapType map(JohnsonType childType) {
		return new MapType(childType, false);
	}

	public static MapType map(JohnsonType childType, boolean nullable) {
		return new MapType(childType, nullable);
	}

	public static RawType raw(boolean nullable) {
		return new RawType(nullable);
	}
}
