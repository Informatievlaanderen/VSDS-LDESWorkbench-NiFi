package be.vlaanderen.informatievlaanderen.ldes.processors.services;

import java.io.ByteArrayOutputStream;

import org.apache.jena.riot.Lang;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.flowfile.attributes.CoreAttributes;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.vlaanderen.informatievlaanderen.ldes.processors.exceptions.ContentRetrievalException;

public class FlowManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(FlowManager.class);

	private static Long counter = -1L;

	private FlowManager() {
	}

	public static String receiveData(ProcessSession session, FlowFile flowFile) {
		return receiveData(session, flowFile, new ByteArrayOutputStream());
	}

	public static String receiveData(ProcessSession session, FlowFile flowFile, ByteArrayOutputStream baos) {
		if (flowFile == null) {
			return null;
		}

		try {
			session.exportTo(flowFile, baos);
			baos.close();

			return baos.toString();
		} catch (Exception e) {
			throw new ContentRetrievalException(flowFile.getId(), e);
		}
	}

	public static void sendRDFToRelation(ProcessSession session, String data, Relationship relationship, Lang lang) {
		sendRDFToRelation(session, session.create(), data, relationship, lang.getContentType().toHeaderString());
	}

	public static void sendRDFToRelation(ProcessSession session, FlowFile flowFile, String data,
			Relationship relationship, Lang lang) {
		sendRDFToRelation(session, flowFile, data, relationship, lang.getContentType().toHeaderString());
	}

	public static void sendRDFToRelation(ProcessSession session, String data, Relationship relationship,
			String contentType) {
		sendRDFToRelation(session, session.create(), data, relationship, contentType);
	}

	public static void sendRDFToRelation(ProcessSession session, FlowFile flowFile, String data,
			Relationship relationship, String contentType) {
		flowFile = session.write(flowFile, out -> {
			out.write(data.getBytes());
		});
		flowFile = session.putAttribute(flowFile, CoreAttributes.MIME_TYPE.key(), contentType);

		session.transfer(flowFile, relationship);

		counter++;

		LOGGER.debug("TRANSFER: sent member #{} (content-type: {})", counter, contentType);
	}
}
