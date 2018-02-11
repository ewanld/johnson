import java.io.IOException;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.github.johnson.examples.vehicle.JsonDto.*;
import com.github.johnson.examples.vehicle.JsonParsers.*;

/**
 * This example demonstrates how to create a parser for the file database1.json, then using this parser to get a DTO
 * (Data Transfer Object) representing the contents of the JSON file.
 */
public class VehicleExample {
	public static void main(String[] args) throws Exception {
		final CatalogDTOParser parser = new CatalogDTOParser(false);
		final JsonFactory jackson = new JsonFactory();
		final ClassLoader classLoader = VehicleExample.class.getClassLoader();
		try (JsonParser jacksonParser = jackson.createParser(classLoader.getResourceAsStream("vehicle1.json"))) {
			final CatalogDTO db = parser.parse(jacksonParser);

			System.out.println("Motorbikes: ");
			for (final MotorbikeDTO t : db.motorbikes) {
				System.out.println(String.format("  - wheel count: %s, cc: %s", t.wheelCount, t.cc));
			}

			System.out.println("Pickups: ");
			for (final PickupDTO t : db.pickups) {
				System.out.println(String.format("  - wheel count: %s, trunk volume: %s, large wheels: %s",
						t.wheelCount, t.trunkVolume, t.largeWheels));
			}
		}
	}
}
