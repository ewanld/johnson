package com.github.johnson.codegen.visitors;

import java.util.HashSet;
import java.util.Set;

import com.github.johnson.codegen.JohnsonTypeVisitor;
import com.github.johnson.codegen.types.ObjectType;
import com.github.johnson.codegen.types.RefType;

public class ListObjectTypes extends JohnsonTypeVisitor {
	private final Set<ObjectType> result = new HashSet<>();

	@Override
	public boolean enterObject(ObjectType type) {
		result.add(type);
		return true;
	}

	@Override
	public boolean enterRef(RefType ref) {
		// avoid infinite recursion
		return !result.contains(ref.getReferencedType());
	}

	public Set<ObjectType> getResult() {
		return result;
	}

}