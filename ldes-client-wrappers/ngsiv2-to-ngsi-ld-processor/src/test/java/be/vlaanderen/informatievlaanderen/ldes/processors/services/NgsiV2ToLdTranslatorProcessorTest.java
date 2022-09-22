package be.vlaanderen.informatievlaanderen.ldes.processors.services;

import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdProcessorRelationships.DATA_OUT_RELATIONSHIP;

import java.io.File;
import java.nio.file.Paths;
import java.util.Objects;

import org.apache.jena.riot.Lang;
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
	
	private final String TEST_DEVICE = "/device/urn:ngsi-v2:cot-imec-be:Device:imec-iow-UR5gEycRuaafxnhvjd9jnU";
	private final String TEST_DEVICE_MODEL = "/device_model/urn:ngsi-v2:cot-imec-be:devicemodel:imec-iow-sensor-v0005";
	private final String TEST_WATER_QUALITY_OBSERVED = "/water_quality_observed/urn:ngsi-v2:cot-imec-be:WaterQualityObserved:dwg-iow-886nJGdroD857YjumSEuNj";
	
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
	void whenProcessorReceivesWaterQualityObservedNgsiV2_thenProcessorTranslatesWaterQualityObservedNgsiV2ToNgsiLd() throws Exception {
		testProcessor("water_quality_observed_ngsiv2.json");
	}
	
	private void testProcessor(String input) throws Exception {
		testRunner.setProperty("CORE_CONTEXT", CORE_CONTEXT);
		testRunner.setProperty("LD_CONTEXT", LD_CONTEXT);
		testRunner.setProperty("DATA_SOURCE_FORMAT", Lang.JSONLD11.getName());
		testRunner.setProperty("DATA_DESTINATION_FORMAT", Lang.NQUADS.getName());
		
		testRunner.enqueue(Paths.get(String.valueOf(new File(Objects.requireNonNull(getClass().getClassLoader().getResource(input)).toURI()))));

        testRunner.run();

        //List<MockFlowFile> dataFlowfiles = testRunner.getFlowFilesForRelationship(DATA_OUT_RELATIONSHIP);

        testRunner.assertQueueEmpty();
		testRunner.assertTransferCount(DATA_OUT_RELATIONSHIP, 1);
	}
}
