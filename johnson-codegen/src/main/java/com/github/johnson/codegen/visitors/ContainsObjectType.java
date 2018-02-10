package com.github.johnson.codegen.visitors;

import com.github.johnson.codegen.JohnsonTypeVisitor;
import com.github.johnson.codegen.types.ObjectType;

public class ContainsObjectType extends JohnsonTypeVisitor {
	private boolean result = false;

	@Override
	public boolean enterObject(ObjectType type) {
		result = true;
		return false;
	}

	public boolean getResult() {
		return result;
	}
}
