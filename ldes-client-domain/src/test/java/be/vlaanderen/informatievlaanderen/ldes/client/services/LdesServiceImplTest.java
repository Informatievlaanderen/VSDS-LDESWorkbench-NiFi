package be.vlaanderen.informatievlaanderen.ldes.client.services;

import static be.vlaanderen.informatievlaanderen.ldes.client.valueobjects.LdesConstants.W3ID_TREE_NODE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import be.vlaanderen.informatievlaanderen.ldes.client.LdesClientImplFactory;
import be.vlaanderen.informatievlaanderen.ldes.client.valueobjects.LdesFragment;

@WireMockTest(httpPort = 10101)
class LdesServiceImplTest {

	private final String initialFragmentUrl = "http://localhost:10101/exampleData?generatedAtTime=2022-05-03T00:00:00.000Z";
	private final String oneMemberFragmentUrl = "http://localhost:10101/exampleData?generatedAtTime=2022-05-05T00:00:00.000Z";
	private final String oneMemberUrl = "http://localhost:10101/member?generatedAtTime=2022-05-05T00:00:00.000Z";

	private LdesService ldesService;

	@BeforeEach
	void setup() {
		ldesService = LdesClientImplFactory.getLdesService(Lang.JSONLD11);

		ldesService.queueFragment(initialFragmentUrl);
	}

	@Test
	void when_processRelations_expectFragmentQueueToBeUpdated() {
		assertEquals(1, ldesService.getStateManager().countQueuedFragments());

		ldesService.extractRelations(getInputModelFromUrl(initialFragmentUrl))
				.forEach(relationStatement -> ldesService.getStateManager().queueFragment(
						relationStatement.getResource().getProperty(W3ID_TREE_NODE).getResource().toString()));

		assertEquals(2, ldesService.getStateManager().countQueuedFragments());
	}

	@Test
	void when_ProcessNextFragmentWith2Fragments_expect2MembersPerFragment() {
		LdesFragment fragment;

		fragment = ldesService.processNextFragment();
		assertEquals(2, fragment.getMembers().size());

		fragment = ldesService.processNextFragment();
		assertEquals(2, fragment.getMembers().size());
	}

	@Test
	void when_ProcessNextFragment_expectValidLdesMember() {
		ldesService = new LdesServiceImpl(Lang.JSONLD11, Lang.NQUADS);
		ldesService.queueFragment(oneMemberFragmentUrl);

		LdesFragment fragment = ldesService.processNextFragment();

		assertEquals(1, fragment.getMembers().size());

		String output = fragment.getMembers().get(0).getMemberData();

		Model outputModel = RDFParserBuilder.create().fromString(output).lang(Lang.NQUADS).toModel();
		Model validateModel = getInputModelFromUrl(oneMemberUrl);

		assertTrue(outputModel.isIsomorphicWith(validateModel));
	}

	private Model getInputModelFromUrl(String fragmentUrl) {
		Model inputModel = ModelFactory.createDefaultModel();

		RDFParser.source(fragmentUrl).forceLang(Lang.JSONLD11).parse(inputModel);

		return inputModel;
	}
}
