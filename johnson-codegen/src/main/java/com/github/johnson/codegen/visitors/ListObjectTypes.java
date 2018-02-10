package com.github.johnson.codegen.visitors;

import java.util.HashSet;
import java.util.Set;

import com.github.johnson.codegen.JohnsonTypeVisitor;
import com.github.johnson.codegen.types.ObjectType;

public class ListObjectTypes extends JohnsonTypeVisitor {
	private final Set<ObjectType> result = new HashSet<>();

	@Override
	public boolean enterObject(ObjectType type) {
		result.add(type);
		return true;
	}

	public Set<ObjectType> getResult() {
		return result;
	}

}