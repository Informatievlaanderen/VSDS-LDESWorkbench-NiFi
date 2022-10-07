package be.vlaanderen.informatievlaanderen.ldes.processors.services;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

import org.apache.jena.riot.Lang;
import org.junit.jupiter.api.Test;

import be.vlaanderen.informatievlaanderen.ldes.processors.valueobjects.MemberInfo;

class OutputFormatConverterTest {
	MemberInfo memberInfo = new MemberInfo(
			"urn:ngsi-v2:cot-imec-be:WaterQualityObserved:imec-iow-3orY3reQDK5n3TMpPnLVYR", "2022-04-19T11:40:42.000Z");

	@Test
	void when_RequestedFormatIsJsonLD11_OutputIsEqualToInput() throws URISyntaxException, IOException {
		String jsonString = getJsonString();
		OutputFormatConverter outputFormatConverter = new OutputFormatConverter(Lang.JSONLD11, true);

		String actualOutput = outputFormatConverter.convertToDesiredOutputFormat(jsonString, memberInfo);

		assertTrue(actualOutput.contains(
				"            \"@id\": \"urn:ngsi-v2:cot-imec-be:WaterQualityObserved:imec-iow-3orY3reQDK5n3TMpPnLVYR/2022-04-19T11:40:42.000Z\","));
	}

	@Test
	void when_RequestedFormatIsNQuads_InputIsConvertedToNQuads() throws URISyntaxException, IOException {
		String jsonString = getJsonString();
		OutputFormatConverter outputFormatConverter = new OutputFormatConverter(Lang.NQUADS, false);

		String actualOutput = outputFormatConverter.convertToDesiredOutputFormat(jsonString, memberInfo);

		assertTrue(actualOutput.contains(
				"<urn:ngsi-v2:cot-imec-be:WaterQualityObserved:imec-iow-3orY3reQDK5n3TMpPnLVYR/2022-04-19T11:40:42.000Z> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://uri.etsi.org/ngsi-ld/default-context/WaterQualityObserved> ."));
	}

	private String getJsonString() throws IOException, URISyntaxException {
		Optional<String> optionalJsonString = Files
				.lines(
						Paths.get(
								Objects.requireNonNull(
										getClass().getClassLoader().getResource("outputformat/example-ldes.json"))
										.toURI()))
				.findFirst();

		return optionalJsonString.orElseThrow(RuntimeException::new);
	}

}