package com.github.johnson.codegen.types;

import com.github.ewanld.visitor.CompositeIterator;
import com.github.johnson.ArrayParser;
import com.github.johnson.codegen.JohnsonTypeVisitor;

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
		return "new ArrayList<>()";
	}

	@Override
	public String getNewParserExpr(boolean _nullable) {
		return String.format("new %s(%s, %s)", getParserTypeName(), Boolean.toString(_nullable),
				childType.getNewParserExpr());
	}

	public void accept(JohnsonTypeVisitor visitor) {
		if (visitor.enterArray(this)) {
			visitor.acceptAny(childType);
		}
		visitor.exitArray(this);
	}

	@Override
	public String getParserTypeName() {
		final String parserClassName = ArrayParser.class.getSimpleName();
		return String.format("%s<%s, %s>", parserClassName, childType.getClassName(), childType.getParserTypeName());
	}

	public JohnsonType getChildType() {
		return childType;
	}

	public String getIteratorExpr(String variableName) {
		if (childType instanceof ArrayType) {
			final ArrayType childType_array = (ArrayType) childType;
			return String.format("%s.fromIteratorOfCollections(%s)", CompositeIterator.class.getSimpleName(),
					childType_array.getIteratorExpr(variableName));
		}
		return variableName + ".iterator()";
	}
}