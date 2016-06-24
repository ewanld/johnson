package com.github.johnson.codegen;

import java.util.Map;

import com.github.johnson.codegen.types.JohnsonType;
import com.github.johnson.codegen.types.RefType;

public class ReplaceRefs extends TypeVisitor {
	private final Map<String, ? extends JohnsonType> types;

	public ReplaceRefs(Map<String, ? extends JohnsonType> typeNames) {
		this.types = typeNames;
	}

	public void replaceAllRefs() {
		for (final JohnsonType type : types.values()) {
			acceptAny(type);
		}
	}
	
	@Override
	public void visitRef(RefType ref) {
		final JohnsonType refType = types.get(ref.getRefTypeName());
		if (refType == null) throw new RuntimeException("missing type: " + ref.getRefTypeName());
		ref.setRefType(refType);
	}
}