package com.github.johnson.codegen.types;

import com.github.johnson.codegen.TypeVisitor;

public class StringType extends PrimitiveType {

	public StringType(boolean nullable) {
		super(nullable);
	}

	public StringType() {
		this(false);
	}

	@Override
	public String getTypeName() {
		return String.class.getSimpleName();
	}

	@Override
	public String getClassName() {
		return String.class.getSimpleName();
	}

	@Override
	public String getJacksonGetterName() {
		return "getValueAsString";
	}

	@Override
	public String getDefaultValueExpr() {
		return "null";
	}

	@Override
	public String getNewParserExpr() {
		return String.format("new StringParser(%s)", Boolean.toString(nullable));
	}

	public void accept(TypeVisitor visitor) {
		visitor.visitString(this);
	}

}