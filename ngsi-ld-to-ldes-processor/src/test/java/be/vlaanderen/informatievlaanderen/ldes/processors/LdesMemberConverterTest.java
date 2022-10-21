package be.vlaanderen.informatievlaanderen.ldes.processors;

import com.jayway.jsonpath.JsonPath;

import be.vlaanderen.informatievlaanderen.ldes.processors.services.LdesMemberConverter;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LdesMemberConverterTest {
	private static final String DEFAULT_ID_JSON_PATH = "$.id";
	private static final String DEFAULT_DELIMITER = "/";
	private static final String DEFAULT_VERSION_OF_KEY = "http://purl.org/dc/terms/isVersionOf";

	LdesMemberConverter ldesMemberConverter = new LdesMemberConverter("",
			DEFAULT_ID_JSON_PATH, DEFAULT_DELIMITER, DEFAULT_VERSION_OF_KEY);

	@ParameterizedTest
	@ArgumentsSource(JsonFileArgumentsProvider.class)
	void shouldMatchCountOfObjects(String fileName, String expectedId) throws IOException, URISyntaxException {
		File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource(fileName)).toURI());
		String jsonString = Files.readString(file.toPath());
		String id = ldesMemberConverter.convert(jsonString);
		assertTrue(((String) JsonPath.read(id, DEFAULT_ID_JSON_PATH)).contains(expectedId));
	}

	static class JsonFileArgumentsProvider implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(
					Arguments.of("example-waterqualityobserved.json",
							"urn:ngsi-v2:cot-imec-be:WaterQualityObserved:imec-iow-3orY3reQDK5n3TMpPnLVYR/"
									+ getPartOfLocalDateTime()),
					Arguments.of("example-device.json",
							"urn:ngsi-v2:cot-imec-be:Device:imec-iow-UR5gEycRuaafxnhvjd9jnU/"
									+ getPartOfLocalDateTime()),
					Arguments.of("example-device-model.json",
							"urn:ngsi-v2:cot-imec-be:devicemodel:imec-iow-sensor-v0005/" + getPartOfLocalDateTime()));
		}

		private String getPartOfLocalDateTime() {
			return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:"));
		}
	}

}