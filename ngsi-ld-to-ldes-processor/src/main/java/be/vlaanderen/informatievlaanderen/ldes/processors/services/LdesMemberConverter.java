package be.vlaanderen.informatievlaanderen.ldes.processors.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
	private final String versionOfKey;
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	public LdesMemberConverter(String dateObservedValueJsonPath, String idJsonPath, String delimiter,
			String versionOfKey) {
		this.dateObservedValueJsonPath = dateObservedValueJsonPath;
		this.idJsonPath = idJsonPath;
		this.delimiter = delimiter;
		this.versionOfKey = versionOfKey;
	}

	public String convert(final String jsonString) {
		String versionObjectId = generateId(jsonString);
		String baseId = JsonPath.read(jsonString, idJsonPath);
		DocumentContext documentContext = JsonPath.using(configuration).parse(jsonString).set(idJsonPath,
				versionObjectId);
		JsonNode versionOfNode = getUriNamedNode(baseId);
		JsonNode versionOf = addVersionOfNode(documentContext, versionOfNode);
		return versionOf.toString();
	}

	private JsonNode getUriNamedNode(String baseId) {
		final ObjectMapper mapper = new ObjectMapper();
		final ObjectNode root = mapper.createObjectNode();
		root.set("@id", mapper.convertValue(baseId, JsonNode.class));
		return root;
	}

	private JsonNode addVersionOfNode(DocumentContext documentContext, JsonNode versionOfNode) {
		ObjectNode json = documentContext.json();
		return json.set(versionOfKey, versionOfNode);
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
