package be.vlaanderen.informatievlaanderen.ldes.processors.services;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LdesMemberConverter {

	private final String dateObservedValueJsonPath;
	private final String idJsonPath;
	private final String delimiter;
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	public LdesMemberConverter(String dateObservedValueJsonPath, String idJsonPath, String delimiter) {
		this.dateObservedValueJsonPath = dateObservedValueJsonPath;
		this.idJsonPath = idJsonPath;
		this.delimiter = delimiter;
	}

	public String convert(final String jsonString) {
		String versionObjectId = generateId(jsonString);
		DocumentContext documentContext = JsonPath.using(configuration).parse(jsonString).set(idJsonPath,
				versionObjectId);
		return documentContext.json().toString();
	}

	public String generateId(String jsonString) {
		String dateObserved;
		try {
			if (dateObservedValueJsonPath.equals("")) {
				throw new PathNotFoundException();
			}
			dateObserved = JsonPath.read(jsonString, dateObservedValueJsonPath);
		} catch (PathNotFoundException pathNotFoundException) {
			dateObserved = LocalDateTime.now().format(formatter);
		}
		String id = JsonPath.read(jsonString, idJsonPath);
		return id + delimiter + dateObserved;
	}

	private static final Configuration configuration = Configuration.builder()
			.jsonProvider(new JacksonJsonNodeJsonProvider())
			.mappingProvider(new JacksonMappingProvider())
			.build();

}
