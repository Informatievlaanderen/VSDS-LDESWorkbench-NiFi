package be.vlaanderen.informatievlaanderen.ldes.processors.services;

import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;

import be.vlaanderen.informatievlaanderen.ldes.client.converters.ModelConverter;
import be.vlaanderen.informatievlaanderen.ldes.processors.valueobjects.MemberInfo;

import java.util.ArrayList;
import java.util.List;

import static org.apache.jena.rdf.model.ResourceFactory.*;

public class OutputFormatConverter {
	private static final String XMLSCHEMA_DATE_TIME = "http://www.w3.org/2001/XMLSchema#dateTime";

	private final Lang outputFormat;
	private final String generatedAtTimeProperty;

	public OutputFormatConverter(Lang outputFormat, String generatedAtTimeProperty) {
		this.outputFormat = outputFormat;
		this.generatedAtTimeProperty = generatedAtTimeProperty;
	}

	public String convertToDesiredOutputFormat(String jsonInput, MemberInfo memberInfo) {
		Model model = ModelConverter.convertStringToModel(jsonInput, Lang.JSONLD11);
		addAdditionalStatements(memberInfo, model);
		return ModelConverter.convertModelToString(model, outputFormat);

	}

	public Lang getOutputFormat() {
		return outputFormat;
	}

	private void addAdditionalStatements(MemberInfo memberInfo, Model model) {
		Resource resource = model.listSubjects().filterKeep(subject -> !subject.isAnon()).nextOptional()
				.orElseThrow(RuntimeException::new);
		List<Statement> statements = new ArrayList<>();
		if (!"".equals(generatedAtTimeProperty))
			statements.add(createStatement(resource, createProperty(generatedAtTimeProperty),
					createTypedLiteral(memberInfo.getObservedAt(),
							TypeMapper.getInstance().getTypeByName(XMLSCHEMA_DATE_TIME))));
		model.add(statements);
	}
}
