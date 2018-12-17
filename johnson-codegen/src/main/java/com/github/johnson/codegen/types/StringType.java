package com.github.johnson.codegen.types;

import com.github.johnson.StringParser;
import com.github.johnson.codegen.JohnsonTypeVisitor;

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
	public String getNewParserExpr(boolean _nullable) {
		return String.format("%s.INSTANCE%s", StringParser.class.getSimpleName(), _nullable ? "_NULLABLE" : "");
	}

	public void accept(JohnsonTypeVisitor visitor) {
		visitor.visitString(this);
	}

}