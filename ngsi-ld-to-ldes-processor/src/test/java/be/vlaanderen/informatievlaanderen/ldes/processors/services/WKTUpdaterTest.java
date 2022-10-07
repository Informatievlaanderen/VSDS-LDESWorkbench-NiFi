package be.vlaanderen.informatievlaanderen.ldes.processors.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Objects;

import org.junit.jupiter.api.Test;

class WKTUpdaterTest {

	WKTUpdater wktUpdater = new WKTUpdater();

	@Test
	void when_GeometryPresent_WKTIsPoint() throws IOException, URISyntaxException {
		File file = new File(Objects
				.requireNonNull(getClass().getClassLoader().getResource("example-waterqualityobserved.json")).toURI());
		String jsonString = Files.readString(file.toPath());
		String wkt = wktUpdater.updateGeoPropertyStatements(jsonString);
		assertTrue(wkt.contains("asWKT"));
	}

	@Test
	void when_noGeometryPresent_WKTisNull() throws IOException, URISyntaxException {
		File file = new File(
				Objects.requireNonNull(getClass().getClassLoader().getResource("example-device-model.json")).toURI());
		String jsonString = Files.readString(file.toPath());
		String wkt = wktUpdater.updateGeoPropertyStatements(jsonString);
		assertFalse(wkt.contains("asWKT"));
	}

}