package com.github.johnson.codegen.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.johnson.RawParser;
import com.github.johnson.codegen.TypeVisitor;

public class RawType extends JohnsonType {

	public RawType(boolean nullable) {
		super(nullable);
	}

	@Override
	public String getClassName() {
		return JsonNode.class.getName();
	}

	@Override
	public String getTypeName() {
		return getClassName();
	}

	@Override
	public String getDefaultValueExpr() {
		return "null";
	}

	@Override
	public String getNewParserExpr() {
		return String.format("new %s(%s)", RawParser.class.getSimpleName(), Boolean.toString(nullable));
	}

	public void accept(TypeVisitor visitor) {
		visitor.visitRaw(this);
	}

}
