package be.vlaanderen.informatievlaanderen.ldes.processors;

import be.vlaanderen.informatievlaanderen.ldes.processors.services.FlowManager;
import be.vlaanderen.informatievlaanderen.ldes.version.services.LdesMemberConverter;
import be.vlaanderen.informatievlaanderen.ldes.version.services.MemberInfoExtractor;
import be.vlaanderen.informatievlaanderen.ldes.version.services.OutputFormatConverter;
import be.vlaanderen.informatievlaanderen.ldes.version.valueobjects.MemberInfo;
import org.apache.jena.riot.Lang;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiLdToLdesMemberProcessorPropertyDescriptors.*;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiLdToLdesMemberProcessorRelationships.DATA_RELATIONSHIP;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiLdToLdesMemberProcessorRelationships.DATA_UNPARSEABLE_RELATIONSHIP;

@Tags({ "ngsild", "ldes", "vsds" })
@CapabilityDescription("Converts NGSI-LD to LdesMembers and send them to the next processor")
public class CreateVersionObjectProcessor extends AbstractProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(CreateVersionObjectProcessor.class);
	private List<PropertyDescriptor> descriptors;
	private Set<Relationship> relationships;
	private LdesMemberConverter ldesMemberConverter;
	private OutputFormatConverter outputFormatConverter;
	private MemberInfoExtractor memberInfoExtractor;

	@Override
	protected void init(final ProcessorInitializationContext context) {
		descriptors = new ArrayList<>();
		descriptors.add(ID_JSON_PATH);
		descriptors.add(DELIMITER);
		descriptors.add(DATE_OBSERVED_VALUE_JSON_PATH);
		descriptors.add(VERSION_OF_KEY);
		descriptors.add(DATA_DESTINATION_FORMAT);
		descriptors.add(GENERATED_AT_TIME_PROPERTY);
		descriptors = Collections.unmodifiableList(descriptors);

		relationships = new HashSet<>();
		relationships.add(DATA_RELATIONSHIP);
		relationships.add(DATA_UNPARSEABLE_RELATIONSHIP);
		relationships = Collections.unmodifiableSet(relationships);
	}

	@Override
	public Set<Relationship> getRelationships() {
		return this.relationships;
	}

	@Override
	public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
		return descriptors;
	}

	@OnScheduled
	public void onScheduled(final ProcessContext context) {
		LOGGER.info("On Schedule");
		String dateObservedValueJsonPath = getDateObservedValueJsonPath(context);
		String idJsonPath = getIdJsonPath(context);
		String delimiter = getDelimiter(context);
		String versionOfKey = getVersionOfKey(context);
		Lang dataDestionationFormat = getDataDestinationFormat(context);
		String generatedAtTimeProperty = getGeneratedAtTimeProperty(context);

		memberInfoExtractor = new MemberInfoExtractor(dateObservedValueJsonPath, idJsonPath);
		ldesMemberConverter = new LdesMemberConverter(dateObservedValueJsonPath, idJsonPath, delimiter);
		outputFormatConverter = new OutputFormatConverter(dataDestionationFormat, generatedAtTimeProperty,
				versionOfKey);
	}

	@Override
	public void onTrigger(final ProcessContext context, final ProcessSession session) {
		LOGGER.info("On Trigger");
		FlowFile flowFile = session.get();

		String memberData;
		String content = FlowManager.receiveData(session, flowFile);
		try {
			MemberInfo memberInfo = memberInfoExtractor.extractMemberInfo(content);
			String convert = ldesMemberConverter.convert(content);
			memberData = outputFormatConverter.convertToDesiredOutputFormat(convert, memberInfo);

			FlowManager.sendRDFToRelation(session, flowFile, memberData, DATA_RELATIONSHIP,
					outputFormatConverter.getOutputFormat());
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			FlowManager.sendRDFToRelation(session, flowFile, content, DATA_UNPARSEABLE_RELATIONSHIP, Lang.JSONLD);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof CreateVersionObjectProcessor))
			return false;
		if (!super.equals(o))
			return false;
		CreateVersionObjectProcessor that = (CreateVersionObjectProcessor) o;
		return Objects.equals(descriptors, that.descriptors) && Objects.equals(relationships, that.relationships)
				&& Objects.equals(ldesMemberConverter, that.ldesMemberConverter)
				&& Objects.equals(outputFormatConverter, that.outputFormatConverter)
				&& Objects.equals(memberInfoExtractor, that.memberInfoExtractor);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), descriptors, relationships, ldesMemberConverter, outputFormatConverter,
				memberInfoExtractor);
	}
}
