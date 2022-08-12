package be.vlaanderen.informatievlaanderen.ldes.processors;

import static be.vlaanderen.informatievlaanderen.ldes.processors.config.LdesProcessorRelationships.DATA_RELATIONSHIP;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;

@WireMockTest(httpPort=10101)
class LdesClientFragmentProcessorTest {
	
    private TestRunner testRunner;

    @BeforeEach
    public void init() {
        testRunner = TestRunners.newTestRunner(LdesClientFragmentProcessor.class);
    }

    @Test
    void when_runningLdesClientWithConnectedFragments_expectsAllLdesMembers() {
        testRunner.setProperty("DATA_SOURCE_URL",
                "http://localhost:10101/exampleData?generatedAtTime=2022-05-04T00:00:00.000Z");

        testRunner.run(10);

        List<MockFlowFile> dataFlowfiles = testRunner.getFlowFilesForRelationship(DATA_RELATIONSHIP);

        assertEquals(6, dataFlowfiles.size());
    }

    // @TODO
//    @Test
//    void when_runningLdesClientWithFragmentContaining2DifferentLDES_expectsLdesMembersOnlyFromFragmentView() {
//        testRunner.setProperty("DATA_SOURCE_URL", "http://localhost:10101/exampleData?scenario=differentLdes");
//
//        testRunner.run(10);
//        
//        LdesClientFragmentProcessor processor = (LdesClientFragmentProcessor)testRunner.getProcessor();
//        LdesStateManager stateManager = processor.ldesService.getStateManager();
//        
//        LOGGER.info("QUEUE FRAGMENTS: {}", stateManager.countQueuedFragments());
//        LOGGER.info("PROCESSED IMMUTABLE FRAGMENTS: {}", stateManager.countProcessedImmutableFragments());
//        LOGGER.info("PROCESSED MUTABLE FRAGMENTS: {}", stateManager.countProcessedMutableFragments());
//        LOGGER.info("PROCESSED MUTABLE FRAGMENT MEMBERS: {}", stateManager.countProcessedMutableFragmentMembers());
//
//        List<MockFlowFile> dataFlowfiles = testRunner.getFlowFilesForRelationship(DATA_RELATIONSHIP);
//
//        assertEquals(1, dataFlowfiles.size());
//    }
}
