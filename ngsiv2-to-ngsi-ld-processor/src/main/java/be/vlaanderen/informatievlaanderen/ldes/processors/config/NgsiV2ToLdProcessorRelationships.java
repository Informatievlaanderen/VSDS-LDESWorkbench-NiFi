package be.vlaanderen.informatievlaanderen.ldes.processors.config;

import org.apache.nifi.processor.Relationship;

public class NgsiV2ToLdProcessorRelationships {

	private NgsiV2ToLdProcessorRelationships() {
	}

	public static final Relationship DATA_OUT_RELATIONSHIP = new Relationship.Builder().name("outgoing")
			.description("Target processor for data").build();

	public static final Relationship DATA_UNPARSEABLE_RELATIONSHIP = new Relationship.Builder().name("unparseable")
			.description("Unparseable data").build();
}
