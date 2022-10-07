package be.vlaanderen.informatievlaanderen.ldes.processors.config;

import static be.vlaanderen.informatievlaanderen.ldes.processors.NgsiV2ToLdTranslatorDefaults.DEFAULT_CORE_CONTEXT;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.util.StandardValidators;

public class NgsiV2ToLdProcessorProperties {

	private NgsiV2ToLdProcessorProperties() {
	}

	public static final PropertyDescriptor CORE_CONTEXT = new PropertyDescriptor.Builder()
			.name("CORE_CONTEXT")
			.displayName("NGSI LD core context")
			.description("URL of the NGSI LD context")
			.required(false)
			.addValidator(StandardValidators.URL_VALIDATOR)
			.defaultValue(DEFAULT_CORE_CONTEXT)
			.build();

	public static final PropertyDescriptor LD_CONTEXT = new PropertyDescriptor.Builder()
			.name("LD_CONTEXT")
			.displayName("Data-specific NGSI LD context")
			.description("URL of the NGSI LD context for this dataset")
			.required(false)
			.addValidator(StandardValidators.URL_VALIDATOR)
			.build();

	public static String getCoreContext(final ProcessContext context) {
		return context.getProperty(CORE_CONTEXT).getValue();
	}

	public static String getLdContext(final ProcessContext context) {
		return context.getProperty(LD_CONTEXT).getValue();
	}
}
