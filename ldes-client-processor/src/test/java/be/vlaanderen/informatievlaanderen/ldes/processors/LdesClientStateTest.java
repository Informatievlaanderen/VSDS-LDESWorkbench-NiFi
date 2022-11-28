package be.vlaanderen.informatievlaanderen.ldes.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import be.vlaanderen.informatievlaanderen.ldes.client.state.LdesStateManager;

@WireMockTest(httpPort = 10101)
public class LdesClientStateTest {

	private final String fragment3 = "http://localhost:10101/exampleData?generatedAtTime=2022-05-03T00:00:00.000Z";
	private final String fragment4 = "http://localhost:10101/exampleData?generatedAtTime=2022-05-04T00:00:00.000Z";
	private final String fragment5 = "http://localhost:10101/exampleData?generatedAtTime=2022-05-05T00:00:00.000Z";

	private TestRunner testRunner;
	private LdesStateManager stateManager;

	@BeforeEach
	void setup() {
		testRunner = TestRunners.newTestRunner(LdesClient.class);
		testRunner.setThreadCount(1);
		testRunner.setProperty("DATA_SOURCE_URL", fragment3);

		stateManager = ((LdesClient) testRunner.getProcessor()).ldesService.getStateManager();
	}

	@AfterEach
	void tearDown() {
		stateManager.clearState();
	}

	@Test
	void whenRunningOnTriggerOnce_thenExactlyOneImmutableFragmentIsPersisted() {
		tearDown();

		testRunner.run(1);
		assertEquals(1, stateManager.countProcessedImmutableFragments());
		assertEquals(2, stateManager.countProcessedMembers());

		testRunner.run(1);
		assertEquals(2, stateManager.countProcessedImmutableFragments());
		assertEquals(4, stateManager.countProcessedMembers());

		testRunner.run(1);
		assertEquals(3, stateManager.countProcessedImmutableFragments());
		assertEquals(6, stateManager.countProcessedMembers());
	}

	@Test
	void whenProcessingIsPaused_thenProcessorResumesAtLastMutableFragment() {
		testRunner.run(1);
		assertEquals(fragment4, stateManager.next());

		setup();
		assertEquals(fragment4, stateManager.next());

		setup();
		testRunner.run(1);
		assertEquals(fragment5, stateManager.next());
	}
}
