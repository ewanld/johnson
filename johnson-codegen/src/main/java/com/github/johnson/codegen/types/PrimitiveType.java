package com.github.johnson.codegen.types;

public abstract class PrimitiveType extends JohnsonType {

	public PrimitiveType(boolean nullable) {
		super(nullable);
	}

	public abstract String getJacksonGetterName();
}