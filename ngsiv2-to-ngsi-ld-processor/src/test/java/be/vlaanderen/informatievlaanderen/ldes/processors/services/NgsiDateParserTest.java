package be.vlaanderen.informatievlaanderen.ldes.processors.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NgsiDateParserTest {

	private String correct = "2022-09-09T09:10:00.000Z";
	private String incomplete = "2022-09-09T09:10:00.000";

	@Test
	void whenDateIsCorrect_thenItIsPassedUnchanged() {
		assertEquals(correct, NgsiLdDateParser.normaliseDate(correct));
	}

	@Test
	void whenDateNeedsParsing_thenItIsParsed() {
		assertEquals(correct, NgsiLdDateParser.normaliseDate(incomplete));
	}
}
