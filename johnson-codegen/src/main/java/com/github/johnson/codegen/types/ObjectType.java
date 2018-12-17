package com.github.johnson.codegen.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.github.johnson.codegen.JohnsonTypeVisitor;
import com.github.johnson.codegen.ObjectProp;

public class ObjectType extends JohnsonType {
	private final Collection<ObjectProp> properties;
	private final JohnsonType baseType;

	/**
	 * @param nullable
	 *            true if null is an acceptable value for tis type, false otherwise.
	 * @param properties
	 *            The key/value pairs that make this object.
	 * @param baseType
	 *            The base object type, or null if this object does not extend another.
	 */
	public ObjectType(boolean nullable, Collection<ObjectProp> properties, JohnsonType baseType) {
		super(nullable);

		assert properties != null;
		assert properties.size() > 0;

		this.properties = properties;
		this.baseType = baseType;
	}

	public ObjectType(Collection<ObjectProp> properties) {
		this(false, properties, null);
	}

	public ObjectType(boolean nullable, ObjectProp... properties) {
		this(false, Arrays.asList(properties), null);
	}

	public ObjectType(ObjectProp... properties) {
		this(false, Arrays.asList(properties), null);
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
	public String getNewParserExpr(boolean _nullable) {
		return String.format("%sParser.INSTANCE%s", getTypeName(), _nullable ? "_NULLABLE" : "");
	}

	@Override
	public String getParserTypeName() {
		return String.format("%sParser", getTypeName());
	}

	public void accept(JohnsonTypeVisitor visitor) {
		visitor.acceptAny(baseType);
		if (visitor.enterObject(this)) {
			for (final ObjectProp prop : properties) {
				prop.accept(visitor);
			}
		}
		visitor.exitObject(this);
	}

	public Collection<ObjectProp> getBaseProperties() {
		return baseType == null ? Collections.emptyList() : getBaseType().getAllProperties();
	}

	public Collection<ObjectProp> getProperties() {
		return properties;
	}

	/**
	 * Return all properties: properties from the base type + own properties.
	 */
	public Collection<ObjectProp> getAllProperties() {
		if (baseType == null) return properties;

		final List<ObjectProp> res = new ArrayList<>();
		res.addAll(getBaseType().getAllProperties());
		res.addAll(properties);
		return res;
	}

	public ObjectType getBaseType() {
		if (baseType == null) {
			return null;
		} else if (baseType instanceof ObjectType) {
			return (ObjectType) baseType;
		} else if (baseType instanceof RefType && ((RefType) baseType).getReferencedType() instanceof ObjectType) {
			return (ObjectType) ((RefType) baseType).getReferencedType();
		} else {
			throw new RuntimeException(String.format(
					"The base type of object %s (declared with the '$extends' keyword) must be an object!", name));
		}
	}

}