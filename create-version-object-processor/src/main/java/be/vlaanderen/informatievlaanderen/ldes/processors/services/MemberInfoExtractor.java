package be.vlaanderen.informatievlaanderen.ldes.processors.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import be.vlaanderen.informatievlaanderen.ldes.processors.valueobjects.MemberInfo;

public class MemberInfoExtractor {

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	private final String dateObservedValueJsonPath;
	private final String idJsonPath;

	public MemberInfoExtractor(String dateObservedValueJsonPath, String idJsonPath) {
		this.dateObservedValueJsonPath = dateObservedValueJsonPath;
		this.idJsonPath = idJsonPath;
	}

	public MemberInfo extractMemberInfo(String jsonString) {
		String dateObserved;
		try {
			dateObserved = JsonPath.read(jsonString, dateObservedValueJsonPath);
		} catch (PathNotFoundException | IllegalArgumentException exception) {
			dateObserved = LocalDateTime.now().format(formatter);
		}
		String id = JsonPath.read(jsonString, idJsonPath);
		return new MemberInfo(id, dateObserved);
	}
}
