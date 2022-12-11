package be.vlaanderen.informatievlaanderen.ldes.processors;

import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdProcessorProperties.CORE_CONTEXT;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdProcessorProperties.LD_CONTEXT;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdProcessorRelationships.DATA_OUT_RELATIONSHIP;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdProcessorRelationships.DATA_UNPARSEABLE_RELATIONSHIP;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.jena.riot.Lang;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdProcessorProperties;
import be.vlaanderen.informatievlaanderen.ldes.processors.services.FlowManager;
import be.vlaanderen.informatievlaanderen.ldes.processors.services.NgsiV2ToLdTranslatorService;

@Tags({ "ngsiv2-to-ld", "ldes", "vsds" })
@CapabilityDescription("Translate and transform NGSIv2 data to NGSI-LD")
public class NgsiV2ToLdTranslatorProcessor extends AbstractProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(NgsiV2ToLdTranslatorProcessor.class);

	protected NgsiV2ToLdTranslatorService translator;

	protected String coreContext;
	protected String ldContext;
	protected Lang dataSourceFormat;
	protected Lang dataDestinationFormat;

	@Override
	public Set<Relationship> getRelationships() {
		return Set.of(DATA_OUT_RELATIONSHIP, DATA_UNPARSEABLE_RELATIONSHIP);
	}

	@Override
	public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
		return List.of(CORE_CONTEXT, LD_CONTEXT);
	}

	@OnScheduled
	public void onScheduled(final ProcessContext context) {
		LOGGER.info("Started NGSIv2 to NGSI-LD processor");
		coreContext = NgsiV2ToLdProcessorProperties.getCoreContext(context);
		ldContext = NgsiV2ToLdProcessorProperties.getLdContext(context);

		translator = new NgsiV2ToLdTranslatorService(coreContext, ldContext);

		LOGGER.info("NGSIv2 to NGSI-LD transformer processor {} started (core context: {}, ld context: {})",
				context.getName(), coreContext, ldContext);
	}

	@Override
	public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
		LOGGER.info("NGSIv2 to NGSI-LD processor triggered");
		FlowFile flowFile = session.get();
		String data = FlowManager.receiveData(session, flowFile);

		try {
			FlowManager.sendRDFToRelation(session, flowFile, translator.translate(data).toString(),
					DATA_OUT_RELATIONSHIP, Lang.JSONLD);
		} catch (Exception e) {
			FlowManager.sendRDFToRelation(session, flowFile, data, DATA_UNPARSEABLE_RELATIONSHIP, Lang.JSONLD);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ Objects.hash(coreContext, dataDestinationFormat, dataSourceFormat, ldContext, translator);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		NgsiV2ToLdTranslatorProcessor other = (NgsiV2ToLdTranslatorProcessor) obj;
		return Objects.equals(coreContext, other.coreContext)
				&& Objects.equals(dataDestinationFormat, other.dataDestinationFormat)
				&& Objects.equals(dataSourceFormat, other.dataSourceFormat)
				&& Objects.equals(ldContext, other.ldContext) && Objects.equals(translator, other.translator);
	}
}
