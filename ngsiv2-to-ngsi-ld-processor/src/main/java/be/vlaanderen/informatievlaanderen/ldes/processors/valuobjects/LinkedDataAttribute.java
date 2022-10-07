package be.vlaanderen.informatievlaanderen.ldes.processors.valuobjects;

import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_LD_ATTRIBUTE_TYPE;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_LD_ATTRIBUTE_VALUE;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_LD_KEY_OBJECT;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_V2_KEY_DATE_OBSERVED;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_V2_KEY_TIMESTAMP;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_V2_KEY_UNIT_CODE;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.translateKey;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonString;
import org.apache.jena.atlas.json.JsonValue;

import be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping;

public class LinkedDataAttribute {

	private final JsonObject attribute = new JsonObject();

	public void setType(JsonString attributeType) {
		setType(attributeType.value());
	}

	public void setType(String attributeType) {
		attribute.put(translateKey(NGSI_LD_ATTRIBUTE_TYPE), attributeType);
	}

	public void setValue(JsonValue attributeValue) {
		attribute.put(translateKey(NGSI_LD_ATTRIBUTE_VALUE), attributeValue);
	}

	public void setObjectValue(JsonValue attributeObject) {
		attribute.put(translateKey(NGSI_LD_KEY_OBJECT), attributeObject);
	}

	public void setTimestamp(String timestamp) {
		attribute.put(translateKey(NGSI_V2_KEY_TIMESTAMP), timestamp);
	}

	public void setDateObserved(String dateObserved) {
		attribute.put(NgsiV2ToLdMapping.NGSI_LD_KEY_DATE_OBSERVED, dateObserved);
	}

	public void removeDateObserved() {
		attribute.remove(translateKey(NGSI_V2_KEY_DATE_OBSERVED));
	}

	public void setUnitCode(String unitCode) {
		attribute.put(translateKey(NGSI_V2_KEY_UNIT_CODE), unitCode);
	}

	public void addMetadata(String metadataKey, JsonObject metadata) {
		attribute.put(translateKey(metadataKey), metadata);
	}

	public JsonObject toAttribute() {
		return attribute;
	}
}
