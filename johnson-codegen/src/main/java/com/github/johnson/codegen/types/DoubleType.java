package com.github.johnson.codegen.types;

import com.github.johnson.DoubleParser;
import com.github.johnson.codegen.JohnsonTypeVisitor;

public class DoubleType extends PrimitiveType {

	public DoubleType(boolean nullable) {
		super(nullable);
	}

	public DoubleType() {
		this(false);
	}

	@Override
	public String getTypeName() {
		return nullable ? Double.class.getSimpleName() : "double";
	}

	@Override
	public String getClassName() {
		return Double.class.getSimpleName();
	}

	@Override
	public String getJacksonGetterName() {
		return "getDoubleValue";
	}

	@Override
	public String getDefaultValueExpr() {
		return nullable ? "null" : "0d";
	}

	@Override
	public String getNewParserExpr(boolean _nullable) {
		return String.format("%s.INSTANCE%s", DoubleParser.class.getSimpleName(), _nullable ? "_NULLABLE" : "");
	}

	public void accept(JohnsonTypeVisitor visitor) {
		visitor.visitDouble(this);
	}
}