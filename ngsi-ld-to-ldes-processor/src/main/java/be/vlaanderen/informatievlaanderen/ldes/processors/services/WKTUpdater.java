package be.vlaanderen.informatievlaanderen.ldes.processors.services;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.util.Map;

public class WKTUpdater {
	private static final String WKT_DATA_TYPE = "http://www.opengis.net/ont/geosparql#wktLiteral";
	private static final String GEOSPARQL_AS_WKT = "http://www.opengis.net/ont/geosparql#asWKT";

	public String updateGeoPropertyStatements(String jsonString) {
		JsonObject root = JsonParser.parseString(jsonString).getAsJsonObject();
		root.entrySet().forEach(entry -> {
			if (entry.getValue().isJsonObject() && entry.getValue().getAsJsonObject().has("type")
					&& entry.getValue().getAsJsonObject().get("type").getAsString().equals("GeoProperty")) {
				JsonObject jsonObject = new JsonObject();
				jsonObject.add("@value", new JsonPrimitive(
						"POINT(" + getCoordinateFromEntry(entry, 0) + " " + getCoordinateFromEntry(entry, 1) + ")"));
				jsonObject.add("@type", new JsonPrimitive(WKT_DATA_TYPE));
				entry.getValue()
						.getAsJsonObject()
						.add(GEOSPARQL_AS_WKT,
								jsonObject);
			}
		});
		return root.toString();
	}

	private JsonElement getCoordinateFromEntry(Map.Entry<String, JsonElement> e, int i) {
		return e.getValue().getAsJsonObject().get("value").getAsJsonObject().get("coordinates").getAsJsonArray().get(i);
	}
}
