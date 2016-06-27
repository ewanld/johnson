package com.github.johnson.codegen.types;

public abstract class JohnsonType {
	protected final boolean nullable;
	protected String name; // nullable
	protected boolean anonymous;

	public JohnsonType(boolean nullable) {
		this.nullable = nullable;
	}

	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
	}

	public final boolean isAnonymous() {
		return anonymous;
	}

	public final void setAnonymous(boolean anonymous) {
		this.anonymous = anonymous;
	}

	public abstract String getClassName();

	public abstract String getTypeName();

	public abstract String getDefaultValueExpr();

	public abstract String getNewParserExpr();

	public String getParserTypeName() {
		return String.format("JohnsonParser<%s>", getClassName());
	}

	public boolean isNullable() {
		return nullable;
	}
}