package com.github.johnson.codegen.types;

import java.math.BigDecimal;

import com.github.johnson.DecimalParser;
import com.github.johnson.codegen.JohnsonTypeVisitor;

public class DecimalType extends PrimitiveType {

	public DecimalType(boolean nullable) {
		super(nullable);
	}

	public DecimalType() {
		this(false);
	}

	@Override
	public String getTypeName() {
		return BigDecimal.class.getSimpleName();
	}

	@Override
	public String getClassName() {
		return BigDecimal.class.getSimpleName();
	}

	@Override
	public String getJacksonGetterName() {
		return "getDecimalValue";
	}

	@Override
	public String getDefaultValueExpr() {
		return "null";
	}

	@Override
	public String getNewParserExpr(boolean _nullable) {
		return String.format("%s.INSTANCE%s", DecimalParser.class.getSimpleName(), _nullable ? "_NULLABLE" : "");
	}

	public void accept(JohnsonTypeVisitor visitor) {
		visitor.visitDecimal(this);
	}

}