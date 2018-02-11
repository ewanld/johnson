import java.io.IOException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.github.johnson.examples.multiple.JsonDto.*;
import com.github.johnson.examples.multiple.JsonParsers.*;

/**
 * This example demonstrates how to create a parser for the file database1.json, then using this parser to get a DTO
 * (Data Transfer Object) representing the contents of the JSON file.
 */
public class WorldExample {
	public static void main(String[] args) throws Exception {
		final WorldDTOParser parser = new WorldDTOParser(false);
		final JsonFactory jackson = new JsonFactory();
		final ClassLoader classLoader = WorldExample.class.getClassLoader();
		try (JsonParser jacksonParser = jackson.createParser(classLoader.getResourceAsStream("world1.json"))) {
			final WorldDTO db = parser.parse(jacksonParser);

			System.out.println("European countries: ");
			for (final EuropeanCountryDTO t : db.europeanCountries) {
				System.out.println(String.format("  - %s (EU member: %s)", t.name, t.euMember ? "yes" : "no"));
			}

			System.out.println("African countries: ");
			for (final AfricanCountryDTO t : db.africanCountries) {
				System.out.println(String.format("  - %s (AFTZ member: %s)", t.name, t.aftzMember ? "yes" : "no"));
			}
		}
	}
}
