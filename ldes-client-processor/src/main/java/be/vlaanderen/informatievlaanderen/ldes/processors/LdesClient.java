package be.vlaanderen.informatievlaanderen.ldes.processors;

import static be.vlaanderen.informatievlaanderen.ldes.processors.config.LdesProcessorProperties.DATA_DESTINATION_FORMAT;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.LdesProcessorProperties.DATA_SOURCE_FORMAT;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.LdesProcessorProperties.DATA_SOURCE_URL;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.LdesProcessorProperties.FRAGMENT_EXPIRATION_INTERVAL;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.LdesProcessorRelationships.DATA_RELATIONSHIP;

import java.util.List;
import java.util.Set;

import org.apache.jena.riot.Lang;
import org.apache.nifi.annotation.behavior.Stateful;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.state.Scope;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.vlaanderen.informatievlaanderen.ldes.client.LdesClientImplFactory;
import be.vlaanderen.informatievlaanderen.ldes.client.converters.ModelConverter;
import be.vlaanderen.informatievlaanderen.ldes.client.services.LdesService;
import be.vlaanderen.informatievlaanderen.ldes.client.valueobjects.LdesFragment;
import be.vlaanderen.informatievlaanderen.ldes.processors.config.LdesProcessorProperties;
import be.vlaanderen.informatievlaanderen.ldes.processors.services.FlowManager;

@Tags({ "ldes-client, vsds" })
@CapabilityDescription("Extract members from an LDES source and send them to the next processor")
@Stateful(description = "Stores mutable fragments to allow processor restart", scopes = Scope.LOCAL)
public class LdesClient extends AbstractProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(LdesClient.class);

	protected LdesService ldesService = LdesClientImplFactory.getLdesService();

	@Override
	public Set<Relationship> getRelationships() {
		return Set.of(DATA_RELATIONSHIP);
	}

	@Override
	public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
		return List.of(DATA_SOURCE_URL, DATA_SOURCE_FORMAT, DATA_DESTINATION_FORMAT, FRAGMENT_EXPIRATION_INTERVAL);
	}

	@OnScheduled
	public void onScheduled(final ProcessContext context) {
		String dataSourceUrl = LdesProcessorProperties.getDataSourceUrl(context);
		Lang dataSourceFormat = LdesProcessorProperties.getDataSourceFormat(context);
		Long fragmentExpirationInterval = LdesProcessorProperties.getFragmentExpirationInterval(context);

		ldesService.setDataSourceFormat(dataSourceFormat);
		ldesService.setFragmentExpirationInterval(fragmentExpirationInterval);

		ldesService.queueFragment(dataSourceUrl);

		LOGGER.info("LDES extraction processor {} with base url {} (expected LDES source format: {})",
				context.getName(), dataSourceUrl, dataSourceFormat);
	}

	@Override
	public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
		if (ldesService.hasFragmentsToProcess()) {
			Lang dataDestinationFormat = LdesProcessorProperties.getDataDestinationFormat(context);
			LdesFragment fragment = ldesService.processNextFragment();

			// Send the processed members to the next Nifi processor
			fragment.getMembers()
					.forEach(ldesMember -> FlowManager.sendRDFToRelation(session,
							ModelConverter.convertModelToString(ldesMember.getMemberModel(), dataDestinationFormat),
							DATA_RELATIONSHIP, dataDestinationFormat));
		}
	}
}
