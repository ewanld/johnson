import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.github.johnson.examples.database.JsonDto.ColumnDTO;
import com.github.johnson.examples.database.JsonDto.DatabaseDTO;
import com.github.johnson.examples.database.JsonDto.TableDTO;
import com.github.johnson.examples.database.JsonParsers.DatabaseDTOParser;

/**
 * This example demonstrates how to create a parser for the file database1.json, then using this parser to get a DTO
 * (Data Transfer Object) representing the contents of the JSON file.
 */
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
