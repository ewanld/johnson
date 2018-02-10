package com.github.johnson.codegen.types;

import com.github.johnson.codegen.JohnsonTypeVisitor;

public class IntType extends PrimitiveType {

	public IntType(boolean nullable) {
		super(nullable);
	}

	public IntType() {
		this(false);
	}

	@Override
	public String getTypeName() {
		return nullable ? Integer.class.getSimpleName() : "int";
	}

	@Override
	public String getClassName() {
		return Integer.class.getSimpleName();
	}

	@Override
	public String getJacksonGetterName() {
		return "getIntValue";
	}

	@Override
	public String getDefaultValueExpr() {
		return nullable ? "null" : "0";
	}

	@Override
	public String getNewParserExpr(boolean _nullable) {
		return String.format("new IntParser(%s)", Boolean.toString(_nullable));
	}

	public void accept(JohnsonTypeVisitor visitor) {
		visitor.visitInt(this);
	}
}