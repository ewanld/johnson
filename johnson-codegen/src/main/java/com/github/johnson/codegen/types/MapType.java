package com.github.johnson.codegen.types;

import com.github.johnson.MapParser;
import com.github.johnson.codegen.TypeVisitor;

public class MapType extends JohnsonType {
	private final JohnsonType childType;

	public MapType(JohnsonType childType, boolean nullable) {
		super(nullable);
		this.childType = childType;
	}

	@Override
	public String getClassName() {
		return getTypeName();
	}

	@Override
	public String getTypeName() {
		return String.format("Map<String, %s>", childType.getClassName());
	}

	@Override
	public String getDefaultValueExpr() {
		return "null";
	}

	@Override
	public String getNewParserExpr() {
		return String.format("new %s(%s, %s)", getParserTypeName(), Boolean.valueOf(nullable),
				childType.getNewParserExpr());
	}

	public void accept(TypeVisitor visitor) {
		if (visitor.enterMap(this)) {
			visitor.acceptAny(childType);
		}
		visitor.exitMap(this);
	}

	@Override
	public String getParserTypeName() {
		final String parserClassName = MapParser.class.getSimpleName();
		return String.format("%s<%s, %s>", parserClassName, childType.getClassName(), childType.getParserTypeName());
	}

	public JohnsonType getChildType() {
		return childType;
	}
}