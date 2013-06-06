package org.commacq;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.listener.SessionAwareMessageListener;

/**
 * Handles inbound requests for initial data loads. Examines whether the client is subscribed
 * for broadcast or requires a more complex continuous query (CQ). Delivers the required data
 * to the client and, if a CQ, sets up the record of the query so that updates will be handled
 * and delivered to the client.
 * 
 * Incoming messages always have a reply queue set (normally a temporary queue).
 */
public class QueryInboundHandler implements SessionAwareMessageListener<Message> {

	private static Logger logger = LoggerFactory.getLogger(QueryInboundHandler.class);
	
	private final DataManager dataManager;
	
	public QueryInboundHandler(DataManager dataManager) {
		this.dataManager = dataManager;
	}
	
	private ThreadLocal<StringWriter> writerThreadLocal = new ThreadLocal<StringWriter>() {
		@Override
		protected StringWriter initialValue() {
			return new StringWriter(1_000_000);
		}
		
		@Override
		public StringWriter get() {
			StringWriter output = super.get();
			//Clear down the buffer each time
			output.getBuffer().setLength(0);
			return output;
		}
	};
	
	@Override
	public void onMessage(Message message, Session session) throws JMSException {
		logger.debug("Received query message: {}", message);
		String entityName;
		try {
			entityName = message.getStringProperty(MessageFields.entityId);
		} catch (JMSException ex) {
			logger.error("Could not perform query", ex);
			return;
		}
		
		logger.info("Received a query for entity {}", entityName);
		
		CsvCache csvCache = dataManager.getCsvCache(entityName);
		
		if(csvCache == null) {
			logger.warn("Entity does not exist on this server: {}", entityName);
			TextMessage outputMessage = session.createTextMessage();
			outputMessage.setJMSCorrelationID(message.getJMSCorrelationID());
			outputMessage.setStringProperty("error", "Entity does not exist: " + entityName);
			MessageProducer messageProducer = session.createProducer(message.getJMSReplyTo());
			messageProducer.send(outputMessage);
			return;
		}
		
		Writer writer = writerThreadLocal.get();
		
		try {
			csvCache.writeToOutput(writer);
		} catch (IOException ex) {
			//Can't see how this would happen with a StringWriter
			throw new RuntimeException(ex);
		}
		
		TextMessage outputMessage = session.createTextMessage(writer.toString());
		outputMessage.setJMSCorrelationID(message.getJMSCorrelationID());
		
		MessageProducer messageProducer = session.createProducer(message.getJMSReplyTo());
		
		messageProducer.send(outputMessage);
		
		logger.info("Completed query for entity {}", entityName);
	}
	
}
