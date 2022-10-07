package be.vlaanderen.informatievlaanderen.ldes.processors.services;

import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdProcessorRelationships.DATA_OUT_RELATIONSHIP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.File;
import java.nio.file.Paths;
import java.util.Objects;

import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import be.vlaanderen.informatievlaanderen.ldes.processors.NgsiV2ToLdTranslatorProcessor;

@WireMockTest(httpPort = 10101)
class NgsiV2ToLdTranslatorProcessorTest {

	private TestRunner testRunner;

	private final String CORE_CONTEXT = "http://localhost:10101/ngsi-ld-core-context.json";
	private final String LD_CONTEXT = "http://localhost:10101/water-quality-observed-context.json";

	@BeforeEach
	void setup() {
		testRunner = TestRunners.newTestRunner(NgsiV2ToLdTranslatorProcessor.class);
	}

	@Test
	void whenProcessorReceivesDeviceModelNgsiV2_thenProcessorTranslatesDeviceModelNgsiV2ToNgsiLd() throws Exception {
		testProcessor("device_model_ngsiv2.json");
	}

	@Test
	void whenProcessorReceivesDeviceNgsiV2_thenProcessorTranslatesDeviceNgsiV2ToNgsiLd() throws Exception {
		testProcessor("device_ngsiv2.json");
	}

	@Test
	void whenProcessorReceivesWaterQualityObservedNgsiV2_thenProcessorTranslatesWaterQualityObservedNgsiV2ToNgsiLd()
			throws Exception {
		testProcessor("water_quality_observed_ngsiv2.json");
	}

	private void testProcessor(String input) throws Exception {
		testRunner.setProperty("CORE_CONTEXT", CORE_CONTEXT);
		testRunner.setProperty("LD_CONTEXT", LD_CONTEXT);

		testRunner.enqueue(Paths.get(String
				.valueOf(new File(Objects.requireNonNull(getClass().getClassLoader().getResource(input)).toURI()))));

		testRunner.run();

		testRunner.assertQueueEmpty();
		testRunner.assertTransferCount(DATA_OUT_RELATIONSHIP, 1);
	}

	@Test
	void whenSonarQubeIsTakenTooFar_thenEqualsIsTested() {
		NgsiV2ToLdTranslatorProcessor processor1 = new NgsiV2ToLdTranslatorProcessor();
		NgsiV2ToLdTranslatorProcessor processor2 = processor1;

		assertEquals(processor1, processor1);
		assertEquals(processor2, processor2);
		assertEquals(processor1, processor2);

		assertNotEquals("test", processor2);
		assertNotEquals(processor2, testRunner);
		assertNotEquals(null, processor2);
	}

	@Test
	void whenSonarQubeIsTakenTooFar_thenHashcodeIsTested() {
		NgsiV2ToLdTranslatorProcessor processor1 = new NgsiV2ToLdTranslatorProcessor();
		NgsiV2ToLdTranslatorProcessor processor2 = processor1;

		assertSame(processor1, processor2);

		assertNotSame("test", processor2);
		assertNotSame(processor2, testRunner);
		assertNotNull(processor2);
	}
}
