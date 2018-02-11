# johnson
Johnson is a Java source code generator plugin to serialize/deserialize JSON data.
The generated code uses Jackson under the hood.

## Build from source
```
mvn package
```

## Quickstart
* Let's consider the following JSON data that represents the structure of a database:
```json
{
	"name": "my database",
	"tables": [ {
 		"name": "table1",
 		"type": "TABLE",
 		"columns": [ {
 			"name": "id",
 			"typeCode": 1,
 			"typeName": "VARCHAR2",
 			"nullable": false,
 			"size": 100
 		},
 		{
 			"name": "email",
 			"typeCode": 1,
 			"typeName": "VARCHAR2",
 			"nullable": true,
 			"size": 200
 		} ]
	 } ]
}
```

* The first step is to create the schema.

  A Johnson schema is a JSON object, where keys are type names and values are a description of the type.

  Create the file ```database-schema.json``` with the following contents:
```json
{
	"database": {
		"name": "string",
		"tables": [ "table" ]
	},
	"table": {
		"name": "string",
		"type": "string",
		"columns": [ "column" ]
	},
	"column": {
		"name": "string",
		"typeCode": "int",
		"typeName": "string",
		"nullable": "bool",
		"size": "int"
	}
}
```

* Add a dependency to ```johnson-runtime``` to your pom.xml:
```xml
<dependency>
    <groupId>com.github.johnson</groupId>
    <artifactId>johnson-runtime</artifactId>
    <version>0.1.0</version>
</dependency>
```

* Add the johnson code generator plugin to your pom.xml:
```xml
<plugin>
    <groupId>com.github.johnson</groupId>
    <artifactId>johnson-codegen-maven-plugin</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <executions>
        <execution>
            <id>generate-database-sources</id>
            <phase>generate-sources</phase>
            <goals>
                <goal>generate</goal>
            </goals>
            <configuration>
                <schema>${project.basedir}/src/main/resources/database-schema.json</schema>
                <packageName>com.github.johnson.examples.database</packageName>
                <dtoClassNameSuffix>DTO</dtoClassNameSuffix>
            </configuration>
        </execution>
    </executions>
</plugin>
```
* Generate sources:
```
mvn generate-sources
```

The sources are generated in ```target/generated-sources/java``` by default.

* Use the generated parser:
```java
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.github.johnson.examples.database.JsonDto.ColumnDTO;
import com.github.johnson.examples.database.JsonDto.DatabaseDTO;
import com.github.johnson.examples.database.JsonDto.TableDTO;
import com.github.johnson.examples.database.JsonParsers.DatabaseDTOParser;

public class DatabaseExample {
    public static void main(String[] args) throws Exception {
        final DatabaseDTOParser parser = new DatabaseDTOParser(false);
        final JsonFactory jackson = new JsonFactory();
        final ClassLoader classLoader = DatabaseExample.class.getClassLoader();
        try (JsonParser jacksonParser = jackson.createParser(classLoader.getResourceAsStream("database1.json"))) {
            final DatabaseDTO db = parser.parse(jacksonParser);
            System.out.println("Database name: " + db.name);
            for (final TableDTO t : db.tables) {
                System.out.println(String.format("Table %s:", t.name));
                for (final ColumnDTO c : t.columns) {
                    System.out.println("  - Column " + c.name);
                }
            }
        }
    }
}
```

Execution output:
```
Database name: my database
Table table1:
  - Column id
  - Column email
```
