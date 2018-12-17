package com.github.johnson.codegen.visitors;

import java.util.Map;

import com.github.johnson.codegen.JohnsonTypeVisitor;
import com.github.johnson.codegen.types.JohnsonType;
import com.github.johnson.codegen.types.RefType;

/**
 * Fill the "referencedType" field of {@link RefType}s.
 */
public class FillRefs extends JohnsonTypeVisitor {
	private final Map<String, ? extends JohnsonType> types;

	public FillRefs(Map<String, ? extends JohnsonType> typeNames) {
		this.types = typeNames;
	}

	public void fillAllRefs() {
		for (final JohnsonType type : types.values()) {
			acceptAny(type);
		}
	}

	@Override
	public boolean enterRef(RefType ref) {
		final JohnsonType refType = types.get(ref.getRefTypeName());
		if (refType == null) throw new RuntimeException("missing type: " + ref.getRefTypeName());
		ref.setRefType(refType);
		return false;
	}
}