package com.github.johnson.codegen.visitors;

import com.github.johnson.codegen.JohnsonTypeVisitor;
import com.github.johnson.codegen.types.ObjectType;
import com.github.johnson.codegen.types.RefType;

public class AssignNames extends JohnsonTypeVisitor {
	private int counter = 1;

	@Override
	public boolean enterObject(ObjectType type) {
		if (type.getName() == null) {
			type.setName("Anonymous" + counter);
			type.setAnonymous(true);
			counter++;
		}
		return true;
	}

	@Override
	public boolean enterRef(RefType type) {
		return false;
	}
}