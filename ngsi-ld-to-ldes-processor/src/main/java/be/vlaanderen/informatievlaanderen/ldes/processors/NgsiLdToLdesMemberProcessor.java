package be.vlaanderen.informatievlaanderen.ldes.processors;

import be.vlaanderen.informatievlaanderen.ldes.processors.services.*;
import be.vlaanderen.informatievlaanderen.ldes.processors.valueobjects.MemberInfo;
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
public class NgsiLdToLdesMemberProcessor extends AbstractProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(NgsiLdToLdesMemberProcessor.class);

	private final WKTUpdater wktUpdater = new WKTUpdater();

	private List<PropertyDescriptor> descriptors;

	private Set<Relationship> relationships;
	private LdesMemberConverter ldesMemberConverter;
	private OutputFormatConverter outputFormatConverter;
	private MemberInfoExtractor memberInfoExtractor;
	private boolean addWKTProperty;

	@Override
	protected void init(final ProcessorInitializationContext context) {
		descriptors = new ArrayList<>();
		descriptors.add(ID_JSON_PATH);
		descriptors.add(DELIMITER);
		descriptors.add(DATE_OBSERVED_VALUE_JSON_PATH);
		descriptors.add(VERSION_OF_KEY);
		descriptors.add(DATA_DESTINATION_FORMAT);
		descriptors.add(GENERATED_AT_TIME_PROPERTY);
		descriptors.add(ADD_WKT_PROPERTY);
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
		addWKTProperty = isAddWKTProperty(context);

		memberInfoExtractor = new MemberInfoExtractor(dateObservedValueJsonPath, idJsonPath);
		ldesMemberConverter = new LdesMemberConverter(dateObservedValueJsonPath, idJsonPath, delimiter, versionOfKey);
		outputFormatConverter = new OutputFormatConverter(dataDestionationFormat, generatedAtTimeProperty);
	}

	@Override
	public void onTrigger(final ProcessContext context, final ProcessSession session) {
		LOGGER.info("On Trigger");
		FlowFile flowFile = session.get();

		String memberData;
		String content = FlowManager.receiveData(session, flowFile);
		try {

			if (addWKTProperty) {
				content = wktUpdater.updateGeoPropertyStatements(content);
			}

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
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(addWKTProperty, descriptors, ldesMemberConverter,
				memberInfoExtractor, outputFormatConverter, relationships, wktUpdater);
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
		NgsiLdToLdesMemberProcessor other = (NgsiLdToLdesMemberProcessor) obj;
		return addWKTProperty == other.addWKTProperty
				&& Objects.equals(descriptors, other.descriptors)
				&& Objects.equals(ldesMemberConverter, other.ldesMemberConverter)
				&& Objects.equals(memberInfoExtractor, other.memberInfoExtractor)
				&& Objects.equals(outputFormatConverter, other.outputFormatConverter)
				&& Objects.equals(relationships, other.relationships) && Objects.equals(wktUpdater, other.wktUpdater);
	}
}
