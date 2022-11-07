package be.vlaanderen.informatievlaanderen.ldes.processors.config;

import static be.vlaanderen.informatievlaanderen.ldes.client.LdesClientDefaults.DEFAULT_DATA_DESTINATION_FORMAT;
import static be.vlaanderen.informatievlaanderen.ldes.client.LdesClientDefaults.DEFAULT_DATA_SOURCE_FORMAT;
import static be.vlaanderen.informatievlaanderen.ldes.client.LdesClientDefaults.DEFAULT_FRAGMENT_EXPIRATION_INTERVAL;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.util.StandardValidators;

import be.vlaanderen.informatievlaanderen.ldes.validators.RDFLanguageValidator;

public final class LdesProcessorProperties {

	private LdesProcessorProperties() {
	}

	public static final PropertyDescriptor DATA_SOURCE_URL = new PropertyDescriptor.Builder()
			.name("DATA_SOURCE_URL")
			.displayName("Data source url")
			.description("Url to data source")
			.required(true)
			.addValidator(StandardValidators.URL_VALIDATOR)
			.build();

	public static final PropertyDescriptor DATA_SOURCE_FORMAT = new PropertyDescriptor.Builder()
			.name("DATA_SOURCE_FORMAT")
			.displayName("Data source format")
			.description("RDF format identifier of the data source")
			.required(false)
			.addValidator(new RDFLanguageValidator())
			.defaultValue(DEFAULT_DATA_SOURCE_FORMAT.getHeaderString())
			.build();

	public static final PropertyDescriptor DATA_DESTINATION_FORMAT = new PropertyDescriptor.Builder()
			.name("DATA_DESTINATION_FORMAT")
			.displayName("Data destination format")
			.description("RDF format identifier of the data destination")
			.required(false)
			.addValidator(new RDFLanguageValidator())
			.defaultValue(DEFAULT_DATA_DESTINATION_FORMAT.getHeaderString())
			.build();

	public static final PropertyDescriptor FRAGMENT_EXPIRATION_INTERVAL = new PropertyDescriptor.Builder()
			.name("FRAGMENT_EXPIRATION_INTERVAL")
			.displayName("Fragment expiration interval")
			.description(
					"The number of seconds to expire a mutable fragment when the Cache-control header contains no max-age value")
			.required(false)
			.addValidator(StandardValidators.POSITIVE_LONG_VALIDATOR)
			.defaultValue(DEFAULT_FRAGMENT_EXPIRATION_INTERVAL.toString())
			.build();

	public static String getDataSourceUrl(final ProcessContext context) {
		return context.getProperty(DATA_SOURCE_URL).getValue();
	}

	public static Lang getDataSourceFormat(final ProcessContext context) {
		return RDFLanguages.nameToLang(context.getProperty(DATA_SOURCE_FORMAT).getValue());
	}

	public static Lang getDataDestinationFormat(final ProcessContext context) {
		return RDFLanguages.nameToLang(context.getProperty(DATA_DESTINATION_FORMAT).getValue());
	}

	public static Long getFragmentExpirationInterval(final ProcessContext context) {
		return Long.valueOf(context.getProperty(FRAGMENT_EXPIRATION_INTERVAL).getValue());
	}
}
