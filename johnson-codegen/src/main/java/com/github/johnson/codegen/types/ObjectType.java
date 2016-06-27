package com.github.johnson.codegen.types;

import java.util.Arrays;
import java.util.Collection;

import com.github.johnson.codegen.ObjectProp;
import com.github.johnson.codegen.TypeVisitor;

public class ObjectType extends JohnsonType {
	private final Collection<ObjectProp> properties;

	public ObjectType(boolean nullable, Collection<ObjectProp> properties) {
		super(nullable);

		assert properties != null;
		assert properties.size() > 0;

		this.properties = properties;
	}

	public ObjectType(Collection<ObjectProp> properties) {
		this(false, properties);
	}

	public ObjectType(boolean nullable, ObjectProp... properties) {
		this(false, Arrays.asList(properties));
	}

	public ObjectType(ObjectProp... properties) {
		this(false, Arrays.asList(properties));
	}

	@Override
	public String getTypeName() {
		final String res = name.substring(0, 1).toUpperCase() + name.substring(1);
		return res;
	}

	@Override
	public String getClassName() {
		return getTypeName();
	}

	@Override
	public String getDefaultValueExpr() {
		return "null";
	}

	@Override
	public String getNewParserExpr() {
		return String.format("new %sParser(%s)", getTypeName(), Boolean.toString(nullable));
	}

	@Override
	public String getParserTypeName() {
		return String.format("%sParser", getTypeName());
	}

	public void accept(TypeVisitor visitor) {
		if (visitor.enterObject(this)) {
			for (final ObjectProp prop : properties) {
				prop.accept(visitor);
			}
		}
		visitor.exitObject(this);
	}

	public Collection<ObjectProp> getProperties() {
		return properties;
	}
}