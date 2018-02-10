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

	/**
	 * Return the Java expression to create a parser for this type. (e.g "new XXX()")
	 */
	public String getNewParserExpr() {
		return getNewParserExpr(nullable);
	}

	/**
	 * Return the Java expression to create a parser for this type. (e.g "new XXX()").
	 * <p>
	 * The nullability of the type is overriden by the nullability specified in argument.
	 */
	public abstract String getNewParserExpr(boolean _nullable);

	public String getParserTypeName() {
		return String.format("JohnsonParser<%s>", getClassName());
	}

	public boolean isNullable() {
		return nullable;
	}
}