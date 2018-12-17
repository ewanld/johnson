package com.github.johnson.codegen.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.johnson.RawParser;
import com.github.johnson.codegen.JohnsonTypeVisitor;

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
	public String getNewParserExpr(boolean _nullable) {
		return String.format("%s.INSTANCE%s", RawParser.class.getSimpleName(), _nullable ? "_NULLABLE" : "");
	}

	public void accept(JohnsonTypeVisitor visitor) {
		visitor.visitRaw(this);
	}

}
