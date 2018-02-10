package com.github.johnson.codegen.types;

import com.github.johnson.codegen.JohnsonTypeVisitor;

public class RefType extends JohnsonType {
	private JohnsonType referencedType;
	private final String refTypeName;

	public RefType(String refTypeName, boolean nullable) {
		super(nullable);
		this.refTypeName = refTypeName;
	}

	public void setRefType(JohnsonType reftype) {
		this.referencedType = reftype;
	}

	public String getRefTypeName() {
		return refTypeName;
	}

	public void accept(JohnsonTypeVisitor visitor) {
		if (visitor.enterRef(this)) {
			visitor.visitRef(this);
			visitor.acceptAny(referencedType);
		}
		visitor.exitRef(this);
	}

	@Override
	public String getClassName() {
		return referencedType.getClassName();
	}

	@Override
	public String getTypeName() {
		return referencedType.getTypeName();
	}

	@Override
	public String getDefaultValueExpr() {
		return referencedType.getDefaultValueExpr();
	}

	@Override
	public String getNewParserExpr(boolean _nullable) {
		return referencedType.getNewParserExpr(_nullable);
	}

	public JohnsonType getReferencedType() {
		return referencedType;
	}
}