package com.github.johnson.codegen.types;

import com.github.johnson.codegen.JohnsonTypeVisitor;

public class LongType extends PrimitiveType {

	public LongType(boolean nullable) {
		super(nullable);
	}

	public LongType() {
		this(false);
	}

	@Override
	public String getTypeName() {
		return nullable ? Long.class.getSimpleName() : "long";
	}

	@Override
	public String getClassName() {
		return Long.class.getSimpleName();
	}

	@Override
	public String getJacksonGetterName() {
		return "getLongValue";
	}

	@Override
	public String getDefaultValueExpr() {
		return nullable ? "null" : "0L";
	}

	@Override
	public String getNewParserExpr(boolean _nullable) {
		return String.format("new LongParser(%s)", Boolean.toString(_nullable));
	}

	public void accept(JohnsonTypeVisitor visitor) {
		visitor.visitLong(this);
	}
}