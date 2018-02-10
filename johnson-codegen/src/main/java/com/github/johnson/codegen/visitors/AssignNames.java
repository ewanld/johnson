package com.github.johnson.codegen.visitors;

import com.github.johnson.codegen.JohnsonTypeVisitor;
import com.github.johnson.codegen.types.ObjectType;

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
}