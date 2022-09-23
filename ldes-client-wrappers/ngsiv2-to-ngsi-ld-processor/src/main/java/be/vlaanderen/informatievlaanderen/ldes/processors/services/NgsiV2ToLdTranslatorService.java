package be.vlaanderen.informatievlaanderen.ldes.processors.services;

import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_LD_ATTRIBUTE_TYPE;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_LD_ATTRIBUTE_TYPE_DATETIME;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_LD_ATTRIBUTE_TYPE_GEOPROPERTY;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_LD_ATTRIBUTE_TYPE_POSTAL_ADDRESS;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_LD_ATTRIBUTE_TYPE_PROPERTY;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_LD_ATTRIBUTE_TYPE_RELATIONSHIP;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_LD_ATTRIBUTE_VALUE;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_LD_OBJECT_TYPE;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_LD_OBJECT_VALUE;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_V2_KEY_COORDINATES;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_V2_KEY_DATE_CREATED;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_V2_KEY_DATE_MODIFIED;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_V2_KEY_DATE_OBSERVED;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_V2_KEY_ID;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_V2_KEY_LOCATION;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_V2_KEY_METADATA;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_V2_KEY_TIMESTAMP;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_V2_KEY_TYPE;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_V2_KEY_UNIT_CODE;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_V2_KEY_VALUE;

import java.util.List;
import java.util.Map.Entry;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonString;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.riot.Lang;

import be.vlaanderen.informatievlaanderen.ldes.processors.valuobjects.LinkedDataAttribute;
import be.vlaanderen.informatievlaanderen.ldes.processors.valuobjects.LinkedDataModel;

public class NgsiV2ToLdTranslatorService {
	
	private final String coreContext;
	private final Lang dataSourceFormat;

	public NgsiV2ToLdTranslatorService(String coreContext, Lang dataSourceFormat) {
		this.coreContext = coreContext;
		this.dataSourceFormat = dataSourceFormat;
	}

	public LinkedDataModel translate(String data, String ldContext, boolean addWktForGeoJSONProperties) {
		JsonObject parsedData = JSON.parse(data);
		LinkedDataModel model = new LinkedDataModel(dataSourceFormat);

		//model.addContext(List.of(coreContext, ldContext));
		model.addContext(List.of(coreContext));

		String id = parsedData.get(NGSI_V2_KEY_ID).getAsString().value();
		String type = parsedData.get(NGSI_V2_KEY_TYPE).getAsString().value();
		String dateObserved = parsedData.get(NGSI_V2_KEY_DATE_OBSERVED) != null ?
				parsedData.get(NGSI_V2_KEY_DATE_OBSERVED).getAsObject().get(NGSI_V2_KEY_VALUE).getAsString().value() : null;

		for (Entry<String, JsonValue> entry : parsedData.entrySet()) {
			String key = entry.getKey();
			JsonValue attribute = entry.getValue();

			if (key.equalsIgnoreCase(NGSI_V2_KEY_ID)) {
				model.setId(translateId(id, type));
				continue;
			} else if (key.equalsIgnoreCase(NGSI_V2_KEY_TYPE)) {
				model.setType(type);
				continue;
			} else if (key.equalsIgnoreCase(NGSI_V2_KEY_DATE_CREATED)) {
				model.setDateCreated(normaliseDate(attribute.getAsString().value()));
				continue;
			} else if (key.equalsIgnoreCase(NGSI_V2_KEY_DATE_MODIFIED)) {
				model.setDateModified(normaliseDate(attribute.getAsString().value()));
				continue;
			}

			JsonObject objectAttribute = entry.getValue().getAsObject();

			LinkedDataAttribute modelAttribute = new LinkedDataAttribute();
			String attributeType = objectAttribute.get(NGSI_V2_KEY_TYPE) != null ? objectAttribute.getString(NGSI_V2_KEY_TYPE) : NGSI_LD_ATTRIBUTE_TYPE_PROPERTY;

			// PROPERTY ATTRIBUTE
			if (!attributeType.equalsIgnoreCase(NGSI_LD_ATTRIBUTE_TYPE_RELATIONSHIP)) {

				modelAttribute.setValue(objectAttribute.get(NGSI_V2_KEY_VALUE));
				if (key.equalsIgnoreCase(NGSI_V2_KEY_LOCATION)) {
					modelAttribute.setType(NGSI_LD_ATTRIBUTE_TYPE_GEOPROPERTY);
					
					if (addWktForGeoJSONProperties) {
						JsonArray coordinates = objectAttribute.get(NGSI_V2_KEY_VALUE).getAsObject().get(NGSI_V2_KEY_COORDINATES).getAsArray();
						
						modelAttribute.setWkt("POINT(" + coordinates.get(0).getAsNumber().value().toString() + " " + coordinates.get(1).getAsNumber().value().toString() + ")");
					}
				} else {
					modelAttribute.setType(NGSI_LD_ATTRIBUTE_TYPE_PROPERTY);
				}
				
				if (dateObserved != null) {
					modelAttribute.setDateObserved(normaliseDate(dateObserved));
				}
			}
			// RELATIONSHIP ATTRIBUTE
			else {
				modelAttribute.setType(NGSI_LD_ATTRIBUTE_TYPE_RELATIONSHIP);

				if (objectAttribute.get(NGSI_V2_KEY_VALUE).isArray()) {
					JsonArray modelAttributeObject = new JsonArray();
					JsonArray items = objectAttribute.get(NGSI_V2_KEY_VALUE).getAsArray();
					for (JsonValue item : items) {
						modelAttributeObject.add(translateObject(key, item.getAsString().value()));
					}

					modelAttribute.setObjectValue(modelAttributeObject);
				}
				else {
					modelAttribute.setObjectValue(new JsonString(translateObject(key, objectAttribute.get(NGSI_V2_KEY_VALUE).getAsString().value())));
				}
			}

			JsonObject attributeData = new JsonObject();
			if (attributeType.equalsIgnoreCase(NGSI_LD_ATTRIBUTE_TYPE_DATETIME)) {
				attributeData.put(NGSI_LD_OBJECT_TYPE, NGSI_LD_ATTRIBUTE_TYPE_DATETIME);
				attributeData.put(NGSI_LD_OBJECT_VALUE, normaliseDate(objectAttribute.getString(NGSI_V2_KEY_VALUE)));

				modelAttribute.setValue(attributeData);
			}
			else if (attributeType.equalsIgnoreCase(NGSI_LD_ATTRIBUTE_TYPE_POSTAL_ADDRESS)) {
				attributeData.put(NGSI_LD_OBJECT_TYPE, NGSI_LD_ATTRIBUTE_TYPE_POSTAL_ADDRESS);
				modelAttribute.setObjectValue(attributeData);
			}

			JsonObject metadata = objectAttribute.get(NGSI_V2_KEY_METADATA) != null ? objectAttribute.get(NGSI_V2_KEY_METADATA).getAsObject() : null;
			if (metadata != null) {
				for (Entry<String, JsonValue> metadataEntry : metadata.entrySet()) {
					String metadataKey = metadataEntry.getKey();
					JsonValue metadataValue = metadataEntry.getValue();
					
					String metadataPropertyValue = "";
					if (metadataValue.isObject() && metadataValue.getAsObject().get(NGSI_V2_KEY_VALUE) != null) {
						metadataPropertyValue = metadataValue.getAsObject().get(NGSI_V2_KEY_VALUE).getAsString().value();
					}

					if (metadataKey.equalsIgnoreCase(NGSI_V2_KEY_TIMESTAMP)) {
						modelAttribute.setTimestamp(normaliseDate(metadataValue.toString()));
					}
					else if (metadataKey.equalsIgnoreCase(NGSI_V2_KEY_UNIT_CODE)) {
						modelAttribute.setUnitCode(metadataPropertyValue);
					}
					else {
						JsonObject metadataProperty = new JsonObject();

						metadataProperty.put(NGSI_LD_ATTRIBUTE_TYPE, NGSI_LD_ATTRIBUTE_TYPE_PROPERTY);
						metadataProperty.put(NGSI_LD_ATTRIBUTE_VALUE, metadataPropertyValue);

						modelAttribute.addMetadata(metadataKey, metadataProperty);
					}
				}
			}

			model.addAttribute(key, modelAttribute);
		}

		return model;
	}

	private String translateId(String entityId, String entityType) {
		return NgsiLdURIParser.toNgsiLdUri(entityId, entityType);
	}

	private String translateObject(String entityId, String value) {
		return NgsiLdURIParser.toNgsiLdObjectUri(entityId, value);
	}

	private String normaliseDate(String date) {
		return NgsiLdDateParser.normaliseDate(date);
	}
}
