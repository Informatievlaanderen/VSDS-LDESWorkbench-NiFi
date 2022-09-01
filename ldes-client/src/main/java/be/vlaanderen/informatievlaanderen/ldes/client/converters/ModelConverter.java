package be.vlaanderen.informatievlaanderen.ldes.client.converters;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.io.StringWriter;

public class ModelConverter {
	
	private ModelConverter() {}

	public static String convertModelToString(Model model, Lang lang) {
		StringWriter out = new StringWriter();
		
		RDFDataMgr.write(out, model, lang);
		
		return out.toString();
	}
}
