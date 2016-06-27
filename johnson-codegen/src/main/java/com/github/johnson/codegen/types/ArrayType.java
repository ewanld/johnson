package com.github.johnson.codegen.types;

import com.github.johnson.ArrayParser;
import com.github.johnson.codegen.TypeVisitor;

public class ArrayType extends JohnsonType {
	private final JohnsonType childType;

	public ArrayType(JohnsonType childType, boolean nullable) {
		super(nullable);
		this.childType = childType;
	}

	@Override
	public String getTypeName() {
		return String.format("List<%s>", childType.getClassName());
	}

	@Override
	public String getClassName() {
		return getTypeName();
	}

	@Override
	public String getDefaultValueExpr() {
		return "null";
	}

	@Override
	public String getNewParserExpr() {
		return String.format("new %s(%s, %s)", getParserTypeName(), Boolean.toString(nullable),
				childType.getNewParserExpr());
	}

	public void accept(TypeVisitor visitor) {
		if (visitor.enterArray(this)) {
			visitor.acceptAny(childType);
		}
		visitor.exitArray(this);
	}

	@Override
	public String getParserTypeName() {
		final String parserClassName = ArrayParser.class.getSimpleName();
		return String.format("%s<%s, %s>", parserClassName, childType.getClassName(),
				childType.getParserTypeName());
	}

	public JohnsonType getChildType() {
		return childType;
	}
}