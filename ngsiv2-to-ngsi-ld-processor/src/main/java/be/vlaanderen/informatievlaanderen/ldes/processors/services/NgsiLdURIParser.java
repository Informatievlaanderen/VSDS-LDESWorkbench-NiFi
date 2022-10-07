package be.vlaanderen.informatievlaanderen.ldes.processors.services;

import static be.vlaanderen.informatievlaanderen.ldes.processors.NgsiV2ToLdTranslatorDefaults.NGSI_LD_URI_PREFIX;
import static be.vlaanderen.informatievlaanderen.ldes.processors.NgsiV2ToLdTranslatorDefaults.getAllowedSchemes;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import be.vlaanderen.informatievlaanderen.ldes.processors.NgsiV2ToLdTranslatorDefaults;

public class NgsiLdURIParser {

	private NgsiLdURIParser() {
	}

	public static String toNgsiLdUri(String entityId, String entityType) {
		URI uri;
		try {
			uri = new URI(entityId);
		} catch (URISyntaxException e) {
			uri = null;
		}

		if (uri == null || uri.getScheme() == null || !Arrays.asList(getAllowedSchemes()).contains(uri.getScheme())) {
			return NGSI_LD_URI_PREFIX + ":" + entityType + ":" + entityId;
		}

		return entityId;
	}

	public static String toNgsiLdObjectUri(String entityId, String value) {
		String entityType = "";
		if (entityId.toLowerCase().startsWith(NgsiV2ToLdTranslatorDefaults.NGSI_LD_REFERENCE_PREFIX)) {
			entityType = entityId.substring(3);
		}

		return toNgsiLdUri(value, entityType);
	}
}
