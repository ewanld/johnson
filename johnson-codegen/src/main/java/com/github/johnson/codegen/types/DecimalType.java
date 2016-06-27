package com.github.johnson.codegen.types;

import java.math.BigDecimal;

import com.github.johnson.codegen.TypeVisitor;

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
	public String getNewParserExpr() {
		return String.format("new DecimalParser(%s)", Boolean.toString(nullable));
	}

	public void accept(TypeVisitor visitor) {
		visitor.visitDecimal(this);
	}

}