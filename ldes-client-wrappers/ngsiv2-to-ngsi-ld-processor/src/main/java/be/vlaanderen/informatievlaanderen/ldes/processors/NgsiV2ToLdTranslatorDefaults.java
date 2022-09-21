package be.vlaanderen.informatievlaanderen.ldes.processors;

public class NgsiV2ToLdTranslatorDefaults {
	
	private NgsiV2ToLdTranslatorDefaults() {}

	public static final String DEFAULT_CORE_CONTEXT = "https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld";
	public static final String LD_CONTEXT_WATER_QUALITY_OBSERVED = "https://raw.githubusercontent.com/smart-data-models/dataModel.WaterQuality/master/context.jsonld";
	
	private static final String[] NGSI_LD_ALLOWED_URI_SCHEMES = new String[] { "urn", "http", "https" };
	
	public static final String NGSI_LD_URI_PREFIX = "urn:ngsi-ld";
	public static final String NGSI_LD_REFERENCE_PREFIX = "ref";
	
	public static String[] getAllowedSchemes() {
		return NGSI_LD_ALLOWED_URI_SCHEMES;
	}
}
