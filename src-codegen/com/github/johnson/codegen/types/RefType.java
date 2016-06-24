package com.github.johnson.codegen.types;

import com.github.johnson.codegen.TypeVisitor;

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

	public void accept(TypeVisitor visitor) {
		visitor.visitRef(this);
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
	public String getNewParserExpr() {
		return referencedType.getNewParserExpr();
	}

	public JohnsonType getReferencedType() {
		return referencedType;
	}
}