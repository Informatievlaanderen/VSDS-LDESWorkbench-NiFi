package be.vlaanderen.informatievlaanderen.ldes.processors;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static be.vlaanderen.informatievlaanderen.ldes.processors.config.LdesProcessorRelationships.DATA_RELATIONSHIP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WireMockTest(httpPort = 8089)
public class LdesClientTest {

    private TestRunner testRunner;

    @BeforeEach
    public void init() {
        testRunner = TestRunners.newTestRunner(LdesClient.class);
    }

    @Test
    void when_runningLdesClientWithConnectedFragments_expectsAllLdesMembers() {
        testRunner.setProperty("DATASOURCE_URL",
                "http://localhost:8089/exampleData?generatedAtTime=2022-05-04T00:00:00.000Z");

        testRunner.run(10);

        List<MockFlowFile> dataFlowfiles = testRunner.getFlowFilesForRelationship(DATA_RELATIONSHIP);

        assertEquals(dataFlowfiles.size(), 6);
        assertTrue(
                dataFlowfiles.stream().allMatch(x -> new String(x.getData()).contains("https://w3id.org/tree#member")));
    }

    @Test
    void when_runningLdesClientWithFragmentContaining2DifferentLDES_expectsLdesMembersOnlyFromFragmentView() {
        testRunner.setProperty("DATASOURCE_URL", "http://localhost:8089/exampleData?scenario=differentLdes");

        testRunner.run(10);

        List<MockFlowFile> dataFlowfiles = testRunner.getFlowFilesForRelationship(DATA_RELATIONSHIP);

        assertEquals(dataFlowfiles.size(), 1);
        assertTrue(
                dataFlowfiles.stream().allMatch(x -> new String(x.getData()).contains("https://w3id.org/tree#member")));
    }
}
