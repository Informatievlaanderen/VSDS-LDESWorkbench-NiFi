package be.vlaanderen.informatievlaanderen.ldes.processors.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.nifi.processor.Processor;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.MockProcessSession;
import org.apache.nifi.util.SharedSessionState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import be.vlaanderen.informatievlaanderen.ldes.processors.exceptions.ContentRetrievalException;

class FlowManagerTest {

	private static final String CONTENT = "data of mock";

	private MockProcessSession session;
	private MockFlowFile flowFile;

	@BeforeEach
	void setUp() {
		final Processor processor = Mockito.mock(Processor.class);
		final AtomicLong idGenerator = new AtomicLong(0L);

		session = new MockProcessSession(new SharedSessionState(processor, idGenerator), processor);
		flowFile = session.create();
	}

	@Test
	void whenFlowFileIsNull_thenFlowManagerReceiveDataReturnsNull() {
		flowFile = null;

		assertNull(FlowManager.receiveData(session, flowFile));
	}

	@Test
	void when_ContentCanBeRead_ContentIsReturned() {
		flowFile.setData(CONTENT.getBytes(StandardCharsets.UTF_8));

		String actualContent = FlowManager.receiveData(session, flowFile);

		assertEquals(CONTENT, actualContent);
	}

	@Test
	void when_RetrievingOfContentThrowsIOException_ContentRetrievalExceptionIsThrown() throws IOException {
		flowFile.setData(CONTENT.getBytes(StandardCharsets.UTF_8));

		ByteArrayOutputStream baos = mock(ByteArrayOutputStream.class);
		doThrow(new IOException()).when(baos).close();

		ContentRetrievalException contentRetrievalException = assertThrows(
				ContentRetrievalException.class,
				() -> FlowManager.receiveData(session, flowFile, baos));

		assertEquals("Content of Flowfile 0 cannot be retrieved", contentRetrievalException.getMessage());
	}

	// @TODO
	// @Test
	// void whenSendingData_thenDataIsWrittenToFlowFile() {
	// Relationship relationship = (new
	// Relationship.Builder()).name("test").build();
	// relationship = Relationship.SELF;
	//
	//
	// FlowManager.sendRDFToRelation(session, flowFile, CONTENT, relationship);
	//
	// assertEquals(CONTENT.getBytes(), flowFile.getData());
	// flowFile.assertAttributeExists(CoreAttributes.MIME_TYPE.key());
	// flowFile.assertAttributeEquals(CoreAttributes.MIME_TYPE.key(),
	// CONTENT_TYPE_JSON);
	// }
}
