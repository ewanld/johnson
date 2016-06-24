package com.github.johnson.codegen;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.omg.CORBA.Request;

import com.github.johnson.JohnsonParser;
import com.github.johnson.codegen.types.JohnsonType;
import com.github.johnson.codegen.types.ObjectType;
import com.github.johnson.util.Maybe;

import java.util.Set;

public class CodeGenerator {
	private final String packageName;
	private final String outputDir;
	private final Map<String /* type name */, JohnsonType> namedTypes;

	public CodeGenerator(String outputDir, String packageName, Map<String, JohnsonType> namedTypes) {
		this.outputDir = outputDir;
		this.packageName = packageName;
		this.namedTypes = new HashMap<>(namedTypes);
		new ReplaceRefs(this.namedTypes).replaceAllRefs();
	}

	private void assignNames() {
		for (final Entry<String, JohnsonType> e : namedTypes.entrySet()) {
			e.getValue().setName(e.getKey());
		}
	}

	private void assignNamesToAnonymousObjectTypes(Collection<ObjectType> objectTypes) {
		final AssignNames assignNames = new AssignNames();
		for (final ObjectType objectType : objectTypes) {
			objectType.accept(assignNames);
		}
	}

	/**
	 * Utility method.
	 */
	private static Set<ObjectType> keepObjectTypes(Collection<? extends JohnsonType> types) {
		final Set<ObjectType> res = new HashSet<ObjectType>();
		for (final JohnsonType type : types) {
			if (type instanceof ObjectType) {
				res.add((ObjectType) type);
			}
		}
		return res;
	}

	public void gen() throws IOException {
		final String dtoClassName = "JsonDto";

		assignNames();
		final Set<ObjectType> objectTypes = listObjectTypes_deep(namedTypes.values());
		assignNamesToAnonymousObjectTypes(objectTypes);
		final Set<JohnsonType> anonAndNamedTypes = new HashSet<JohnsonType>(namedTypes.values());
		anonAndNamedTypes.addAll(objectTypes);

		final DtoWriter dto = new DtoWriter(outputDir, packageName, dtoClassName, objectTypes);
		final ParsersWriter parsers = new ParsersWriter(outputDir, packageName, "JsonParsers", dtoClassName,
				anonAndNamedTypes);
		parsers.gen();
		dto.gen();
		dto.close();
		parsers.close();
	}

	private static Set<ObjectType> listObjectTypes_deep(Collection<? extends JohnsonType> types) {
		final ListObjectTypes visitor = new ListObjectTypes();
		for (final JohnsonType type : types) {
			visitor.acceptAny(type);
		}
		return visitor.getResult();
	}

	private static class ParsersWriter extends JavaWriter {
		private final String dtoClassName;
		private final Set<? extends JohnsonType> namedTypes;

		public ParsersWriter(String outputDir, String packageName, String className, String dtoClassName,
				Collection<? extends JohnsonType> namedTypes) throws IOException {
			super(outputDir, packageName, className);
			this.dtoClassName = dtoClassName;
			this.namedTypes = new HashSet<>(namedTypes);
		}

		public void gen() throws IOException {
			genHeader();
			final Collection<ObjectType> objectTypes = keepObjectTypes(namedTypes);
			for (final ObjectType type : objectTypes) {
				genObjectParser(type);
			}
			genFooter();
		}

		private void genObjectParser(ObjectType objectType) throws IOException {
			final String dtoClassName = objectType.getTypeName();
			writeln("	public static class %sParser extends %s<%s> {", dtoClassName,
					JohnsonParser.class.getSimpleName(), dtoClassName);

			// generate private fields
			for (final ObjectProp prop : objectType.getProperties()) {
				writeln("		public final %s parser_%s = %s;", prop.getType().getParserTypeName(),
						prop.getJavaName(), prop.getType().getNewParserExpr());
			}
			writeln();

			// generate constructor
			writeln("		public %sParser(boolean nullable) {", dtoClassName);
			writeln("			super(nullable);");
			writeln("		}\n");

			// generate method doParse
			writeln("		@Override");
			writeln("		protected %s doParse(JsonParser jp) throws JsonParseException, IOException {",
					dtoClassName);
			writeln("			assert jp.getCurrentToken() == JsonToken.START_OBJECT;\n");

			// create var_* variables
			for (final ObjectProp prop : objectType.getProperties()) {
				writeln("			Maybe<%s> val_%s = Maybe.empty();", prop.getType().getClassName(),
						prop.getJavaName(), prop.getDefaultValueExpr());
			}
			writeln();

			writeln("			while (jp.nextToken() != JsonToken.END_OBJECT) {");
			writeln("				assert jp.getCurrentToken() == JsonToken.FIELD_NAME;");
			writeln("				final String fieldName = jp.getCurrentName();");
			writeln("				jp.nextToken();");

			boolean first = true;
			for (final ObjectProp prop : objectType.getProperties()) {
				writeln("				%sif (fieldName.equals(\"%s\")) {", first ? "" : "else ",
						toJavaLiteral(prop.getName()));
				writeln("					val_%s = Maybe.of(parser_%s.parse(jp));", prop.getJavaName(),
						prop.getJavaName());
				writeln("				}");
				first = false;
			}
			writeln("				else {");
			writeln("					throw new JsonParseException(jp, \"unknown field: \" + fieldName);");
			writeln("				}"); // end if
			writeln();
			writeln("			}"); // end while

			// check that all required fields are set
			writeln();
			for (final ObjectProp prop : objectType.getProperties()) {
				if (prop.isRequired()) {
					writeln("			if (!val_%s.isPresent()) throw new JsonParseException(jp, \"A required property is missing: %s\");",
							prop.getJavaName(), toJavaLiteral(prop.getName()));
				}
			}
			writeln();

			// assign res value
			write("			final %s res = new %s(", dtoClassName, dtoClassName);
			first = true;
			for (final ObjectProp prop : objectType.getProperties()) {
				write("%sval_%s%s", first ? "" : ", ", prop.getJavaName(), prop.isRequired() ? ".get()" : "");
				first = false;
			}
			writeln(");");

			writeln("			return res;");
			writeln("		}"); // end method parse

			// generate method serialize
			writeln("		@Override");
			writeln("		public void serialize(%s value, JsonGenerator generator) throws IOException {",
					objectType.getClassName());
			writeln("			generator.writeStartObject();");
			for (final ObjectProp prop : objectType.getProperties()) {
				if (prop.isRequired()) {
					writeln("			generator.writeFieldName(\"%s\");", toJavaLiteral(prop.getName()));
					writeln("			parser_%s.serialize(value.%s, generator);", prop.getJavaName(),
							prop.getJavaName());
				} else {
					writeln("			if (value.%s.isPresent()) {", prop.getJavaName());
					writeln("				generator.writeFieldName(\"%s\");", toJavaLiteral(prop.getName()));
					writeln("				parser_%s.serialize(value.%s.get(), generator);", prop.getJavaName(),
							prop.getJavaName());
					writeln("			}");
				}
			}
			writeln("			generator.writeEndObject();");
			writeln("		}");

			writeln("	}\n"); // end class
		}

		private static String toJavaLiteral(String s) {
			return s.replace("\\", "\\\\").replace("\"", "\\\"");
		}

		private void genHeader() throws IOException {
			writeln("package %s;", packageName);
			writeln();
			writeln("import java.io.IOException;");
			writeln("import java.util.List;");
			writeln("import java.util.Map;");
			writeln("import java.math.BigDecimal;");
			writeln();
			writeln("import com.github.johnson.*;");
			writeln("import %s;", Maybe.class.getName());
			writeln();
			writeln("import com.fasterxml.jackson.core.JsonParseException;");
			writeln("import com.fasterxml.jackson.core.JsonParser;");
			writeln("import com.fasterxml.jackson.core.JsonGenerator;");
			writeln("import com.fasterxml.jackson.core.JsonToken;");
			writeln();
			writeln("import %s.%s.*;", packageName, dtoClassName);
			writeln();
			writeln("public class %s {", className);
		}

		private void genFooter() throws IOException {
			writeln("}");
		}

	}

	static String toJavaFieldName(String identifier) {
		final StringBuilder res = new StringBuilder();
		if (Character.isDigit(identifier.charAt(0))) {
			res.append("_");
		}
		final String javaName = identifier.replaceAll("[^\\w]", "_");
		res.append(javaName.substring(0, 1).toLowerCase());
		res.append(javaName.substring(1));
		return res.toString();
	}

	public static String toJavaClassName(String identifier) {
		final String res = identifier.substring(0, 1).toUpperCase() + identifier.substring(1);
		return res;
	}

	private static class DtoWriter extends JavaWriter {
		private final Set<ObjectType> types;

		public DtoWriter(String outputDir, String packageName, String className, Set<ObjectType> types)
				throws IOException {
			super(outputDir, packageName, className);
			this.types = types;
		}

		public void gen() throws IOException {
			genHeader();
			genDtoClasses();
			genFooter();
		}

		private void genDtoClasses() throws IOException {
			for (final ObjectType type : types) {
				genDtoClass(type);
			}
		}

		private void genDtoClass(ObjectType type) throws IOException {
			// @formatter:off
			writeln("	public static class %s {", type.getTypeName());
			for (final ObjectProp p : type.getProperties()) {
				final String javaFieldName = toJavaFieldName(p.getName());
				writeln("		public final %s %s;", p.getTypeName(), javaFieldName);
			}

			// constructor
			writeln();
			write("		public %s(", type.getTypeName());
			boolean first = true;
			for (final ObjectProp p : type.getProperties()) {
				final String javaFieldName = toJavaFieldName(p.getName());
				write("%s%s %s", first ? "" : ", ", p.getTypeName(), javaFieldName);
				first = false;
			}
			writeln(") {");
			for (final ObjectProp p : type.getProperties()) {
				final String javaFieldName = toJavaFieldName(p.getName());
				writeln("			this.%s = %s;", javaFieldName, javaFieldName);
			}
			writeln("		}");
			writeln("	}\n");
			// @formatter:on
		}

		private void genHeader() throws IOException {
			writeln("package %s;\n", packageName);
			writeln("import %s;", List.class.getName());
			writeln("import %s;", Map.class.getName());
			writeln("import %s;", BigDecimal.class.getName());
			writeln("import %s;", Maybe.class.getName());
			writeln();
			writeln("public class %s {", className);
		}

		private void genFooter() throws IOException {
			writeln("}");

		}
	}

	private static class JavaWriter implements Closeable {
		protected final FileWriter wrapped;
		protected final String packageName;
		protected final String className;

		public JavaWriter(String outputDir, String packageName, String className) throws IOException {
			this.packageName = packageName;
			this.className = className;

			final String srcDir = outputDir + "/" + packageName.replace('.', '/');
			new File(srcDir).mkdirs();
			final String file = String.format("%s/%s.java", srcDir, className);
			wrapped = new FileWriter(file);
		}

		protected void writeln(String format, Object... args) throws IOException {
			wrapped.write(String.format(format + "\n", args));
		}

		protected void writeln() throws IOException {
			wrapped.write("\n");
		}

		protected void write(String format, Object... args) throws IOException {
			wrapped.write(String.format(format, args));
		}

		@Override
		public void close() throws IOException {
			wrapped.close();
		}

	}

	public static class AssignNames extends TypeVisitor {
		private int counter = 1;

		@Override
		public boolean enterObject(ObjectType type) {
			if (type.getName() == null) {
				type.setName("Anonymous" + counter);
				type.setAnonymous(true);
				counter++;
			}
			return true;
		}
	}

	public static class ListObjectTypes extends TypeVisitor {
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
}
