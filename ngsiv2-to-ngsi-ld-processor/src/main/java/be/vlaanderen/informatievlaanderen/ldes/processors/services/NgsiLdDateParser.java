package be.vlaanderen.informatievlaanderen.ldes.processors.services;

public class NgsiLdDateParser {

	private NgsiLdDateParser() {
	}

	public static String normaliseDate(String date) {
		if (!date.endsWith("Z")) {
			return date + "Z";
		}

		return date;
	}
}
