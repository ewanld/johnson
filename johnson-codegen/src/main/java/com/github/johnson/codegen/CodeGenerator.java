package com.github.johnson.codegen;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.github.visitorj.VisitEvent;
import com.github.visitorj.VisitResult;
import com.github.visitorj.Visitable;
import com.github.visitorj.VisitableList;
import com.github.visitorj.codegen.JavaClass;
import com.github.visitorj.codegen.VisitorGeneratorService;
import com.github.visitorj.util.*;
import com.github.johnson.JohnsonParser;
import com.github.johnson.codegen.types.JohnsonType;
import com.github.johnson.codegen.types.ObjectType;
import com.github.johnson.codegen.visitors.AssignNames;
import com.github.johnson.codegen.visitors.ContainsObjectType;
import com.github.johnson.codegen.visitors.ListObjectTypes;
import com.github.johnson.codegen.visitors.ReplaceRefs;
import com.github.johnson.util.Maybe;

public class CodeGenerator {
	private final String packageName;
	private final String outputDir;
	private final Map<String /* type name */, JohnsonType> namedTypes;
	private boolean generateDtoEmptyConstructor = false;
	private boolean generateDtoVisitor;
	private boolean dtoFieldsFinal;
	private String dtoVisitorName = "JsonDto";
	private String dtoClassNameSuffix = "";

	public CodeGenerator(String outputDir, String packageName, Map<String, JohnsonType> namedTypes) {
		this.outputDir = outputDir;
		this.packageName = packageName;
		this.namedTypes = new HashMap<>(namedTypes);
		new ReplaceRefs(this.namedTypes).replaceAllRefs();
	}

	public void setDtoClassNameSuffix(String dtoClassNameSuffix) {
		this.dtoClassNameSuffix = dtoClassNameSuffix == null ? "" : dtoClassNameSuffix;
	}

	public void setGenerateDtoEmptyConstructor(boolean generateDtoEmptyConstructor) {
		this.generateDtoEmptyConstructor = generateDtoEmptyConstructor;
	}

	public void setGenerateDtoVisitor(boolean generateDtoVisitor) {
		this.generateDtoVisitor = generateDtoVisitor;
	}

	public void setDtoVisitorName(String dtoVisitorName) {
		this.dtoVisitorName = dtoVisitorName;
	}

	public void setDtoFieldsFinal(boolean dtoFieldsFinal) {
		this.dtoFieldsFinal = dtoFieldsFinal;
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
	 * Utility method. Return only the 'ObjectType' types from the specified collection of types.
	 */
	private static Set<ObjectType> keepObjectTypes(Collection<? extends JohnsonType> types) {
		final Set<ObjectType> res = new HashSet<>();
		for (final JohnsonType type : types) {
			if (type instanceof ObjectType) {
				res.add((ObjectType) type);
			}
		}
		return res;
	}

	public void gen() throws IOException {
		final String dtoClassName = "JsonDto";
		final String parsersClassName = "JsonParsers";

		assignNames();
		final Set<ObjectType> objectTypes = listObjectTypes_deep(namedTypes.values());
		assignNamesToAnonymousObjectTypes(objectTypes);
		objectTypes.forEach(t -> t.setName(t.getName() + dtoClassNameSuffix));
		final Set<JohnsonType> anonAndNamedTypes = new HashSet<>(namedTypes.values());
		anonAndNamedTypes.addAll(objectTypes);

		final DtoWriter dto = new DtoWriter(outputDir, packageName, dtoClassName, objectTypes,
				generateDtoEmptyConstructor, generateDtoVisitor, dtoFieldsFinal, dtoVisitorName);
		final ParsersWriter parsers = new ParsersWriter(outputDir, packageName, parsersClassName, dtoClassName,
				anonAndNamedTypes);
		parsers.gen();
		parsers.close();

		dto.gen();
		dto.close();

		if (generateDtoVisitor) {
			// we need to keep only ObjectTypes because a named type may be an alias to a primitive type
			final List<JavaClass> javaClasses = namedTypes.values().stream().filter(j -> j instanceof ObjectType)
					.map(t -> new JavaClass(packageName + "." + dtoClassName + "." + t.getClassName()))
					.collect(Collectors.toList());
			new VisitorGeneratorService().generateAll(dtoVisitorName, new File(outputDir), javaClasses, packageName);
		}
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
						prop.getJavaName());
			}
			writeln();

			writeln("			while (jp.nextToken() != JsonToken.END_OBJECT) {");
			writeln("				assert jp.getCurrentToken() == JsonToken.FIELD_NAME;");
			writeln("				final String fieldName = jp.getCurrentName();");
			writeln("				jp.nextToken();");

			if (!objectType.getProperties().isEmpty()) {
				boolean first = true;
				for (final ObjectProp prop : objectType.getProperties()) {
					write("				");
					if (!first) write("else ");
					writeln("if (fieldName.equals(\"%s\")) {", escapeJavaString(prop.getName()));
					writeln("					val_%s = Maybe.of(parser_%s.parse(jp));", prop.getJavaName(),
							prop.getJavaName());
					writeln("				}");
					first = false;
				}
				writeln("				else {");
				writeln("					throw new JsonParseException(jp, \"unknown field: \" + fieldName);");
				writeln("				}"); // end if
			}
			writeln();
			writeln("			}"); // end while

			// check that all required fields are set
			writeln();
			for (final ObjectProp prop : objectType.getProperties()) {
				if (prop.isRequired()) {
					writeln("			if (!val_%s.isPresent()) throw new JsonParseException(jp, \"A required property is missing: %s\");",
							prop.getJavaName(), escapeJavaString(prop.getName()));
				}
			}
			writeln();

			// assign res value
			write("			final %s res = new %s(", dtoClassName, dtoClassName);
			boolean first = true;
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
					writeln("			generator.writeFieldName(\"%s\");", escapeJavaString(prop.getName()));
					writeln("			parser_%s.serialize(value.%s, generator);", prop.getJavaName(),
							prop.getJavaName());
				} else {
					writeln("			if (value.%s.isPresent()) {", prop.getJavaName());
					writeln("				generator.writeFieldName(\"%s\");", escapeJavaString(prop.getName()));
					writeln("				parser_%s.serialize(value.%s.get(), generator);", prop.getJavaName(),
							prop.getJavaName());
					writeln("			}");
				}
			}
			writeln("			generator.writeEndObject();");
			writeln("		}");

			writeln("	}\n"); // end class
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

	private static class DtoWriter extends JavaWriter {
		private final SortedSet<ObjectType> types = new TreeSet<>(Comparator.comparing(JohnsonType::getName));
		private final boolean generateDtoEmptyConstructor;
		private final boolean generateDtoVisitor;
		private final boolean dtoFieldsFinal;
		private final String dtoVisitorName;

		public DtoWriter(String outputDir, String packageName, String className, Set<ObjectType> types,
				boolean generateDtoEmptyConstructor, boolean generateDtoVisitor, boolean dtoFieldsFinal,
				String dtoVisitorName) throws IOException {
			super(outputDir, packageName, className);
			this.types.addAll(types);
			this.generateDtoEmptyConstructor = generateDtoEmptyConstructor;
			this.generateDtoVisitor = generateDtoVisitor;
			this.dtoFieldsFinal = dtoFieldsFinal;
			this.dtoVisitorName = dtoVisitorName;
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
			write("	public static class %s ", type.getTypeName());
			if (generateDtoVisitor) {
				write("implements Visitable<%sVisitor> ", dtoVisitorName);
			}
			writeln("{");
			// declare fields
			for (final ObjectProp p : type.getProperties()) {
				final String javaFieldName = p.getJavaName();
				writeln("		public %s%s %s;", dtoFieldsFinal ? "final " : "", p.getTypeName(), javaFieldName);
			}

			// constructor
			writeln();
			write("		public %s(", type.getTypeName());
			boolean first = true;
			for (final ObjectProp p : type.getProperties()) {
				final String javaFieldName = p.getJavaName();
				write("%s%s %s", first ? "" : ", ", p.getTypeName(), javaFieldName);
				first = false;
			}
			writeln(") {");
			for (final ObjectProp p : type.getProperties()) {
				final String javaFieldName = p.getJavaName();
				writeln("			this.%s = %s;", javaFieldName, javaFieldName);
			}
			writeln("		}");

			// empty constructor (optional)
			if (generateDtoEmptyConstructor && !type.getProperties().isEmpty()) {
				writeln();
				writeln("		public %s() {", type.getTypeName());
				for (final ObjectProp p : type.getProperties()) {
					final String javaFieldName = p.getJavaName();
					writeln("			this.%s = %s;", javaFieldName, p.getDefaultValueExpr());
				}
				writeln("		}");
				writeln();
			}

			// visitor "event" method
			if (generateDtoVisitor) {
				writeln("		@Override");
				writeln("		public void event(VisitEvent event, %sVisitor visitor) {", dtoVisitorName);
				if (!type.isAnonymous()) {
					writeln("			visitor.event(event, this);");
				}
				writeln("		}");
				writeln();
			}

			// visitor "visit" method
			if (generateDtoVisitor) {
				writeln("		@Override");
				writeln("		public final VisitResult visit(final %sVisitor visitor, String identifier) {",
						dtoVisitorName);
				if (type.isAnonymous()) {
					writeln("			return VisitResult.CONTINUE;");
				} else {
					writeln("			return visitor.visit(this, identifier);");
				}
				writeln("		}");
				writeln();
			}

			// visitor "getVisitableChildren" method
			if (generateDtoVisitor) {
				writeln("		@Override");
				writeln("		public final VisitableList<%sVisitor> getVisitableChildren() {", dtoVisitorName);
				writeln("			final VisitableList<%sVisitor> visitableChildren = new VisitableList<>();",
						dtoVisitorName);
				for (final ObjectProp p : type.getProperties()) {
					final ContainsObjectType containsObjectType = new ContainsObjectType();
					containsObjectType.acceptAny(p.getType());
					if (containsObjectType.getResult()) {
						writeln("			visitableChildren.add(%s, %s);", p.getVisitableExpr(),
								toJavaLiteral(p.getJavaName()));
					}
				}
				writeln("			return visitableChildren;");
				writeln("		}");
				writeln();
			}

			writeln("	}\n");

		}

		private void genHeader() throws IOException {
			writeln("package %s;\n", packageName);

			writeln("import %s;", List.class.getName());
			writeln("import %s;", Map.class.getName());
			writeln("import %s;", ArrayList.class.getName());
			writeln("import %s;", BigDecimal.class.getName());
			writeln("import %s;", Maybe.class.getName());
			writeln();

			if (generateDtoVisitor) {
				writeln("import %s;", Visitable.class.getName());
				writeln("import %s;", VisitableList.class.getName());
				writeln("import %s;", VisitEvent.class.getName());
				writeln("import %s;", VisitResult.class.getName());
				writeln("import %s;", Arrays.class.getName());
				writeln("import %s;", CompositeIterator.class.getName());
				writeln("import %s;", Iterator.class.getName());
				writeln("import %s;", ArrayList.class.getName());
				writeln("import %s;", Collection.class.getName());
				writeln("import %s;", Collections.class.getName());
				writeln();
			}

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

	private static String escapeJavaString(String s) {
		return s.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	private static String toJavaLiteral(String s) {
		return "\"" + escapeJavaString(s) + "\"";
	}
}
