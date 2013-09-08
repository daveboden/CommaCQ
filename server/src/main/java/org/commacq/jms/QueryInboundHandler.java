package org.commacq.jms;

import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import lombok.extern.slf4j.Slf4j;

import org.commacq.CsvDataSource;
import org.commacq.CsvDataSourceLayer;
import org.commacq.CsvLineCallbackStringWriter;
import org.springframework.jms.listener.SessionAwareMessageListener;

/**
 * Handles inbound requests for initial data loads. Examines whether the client is subscribed
 * for broadcast or requires a more complex continuous query (CQ). Delivers the required data
 * to the client and, if a CQ, sets up the record of the query so that updates will be handled
 * and delivered to the client.
 * 
 * Incoming messages always have a reply queue set (normally a temporary queue).
 */
@Slf4j
public class QueryInboundHandler implements SessionAwareMessageListener<Message> {
	
	private final CsvDataSourceLayer layer;
	
	public QueryInboundHandler(CsvDataSourceLayer layer) {
		this.layer = layer;
	}
	
	private ThreadLocal<CsvLineCallbackStringWriter> writerThreadLocal = new ThreadLocal<CsvLineCallbackStringWriter>() {
		protected CsvLineCallbackStringWriter initialValue() {
			return new CsvLineCallbackStringWriter();
		};
		
		public CsvLineCallbackStringWriter get() {
			CsvLineCallbackStringWriter writer = super.get();
			writer.clear();
			return writer;
		};
	};
	
	@Override
	public void onMessage(Message message, Session session) throws JMSException {
		log.debug("Received query message: {}", message);
		String entityId;
		try {
			entityId = message.getStringProperty(MessageFields.entityId);
		} catch (JMSException ex) {
			log.error("Could not perform query", ex);
			return;
		}
		
		log.info("Received a query for entity {}", entityId);
		
		CsvDataSource source = layer.getCsvDataSource(entityId);
		
		if(source == null) {
			log.warn("Entity does not exist on this server: {}", entityId);
			TextMessage outputMessage = session.createTextMessage();
			outputMessage.setJMSCorrelationID(message.getJMSCorrelationID());
			outputMessage.setStringProperty("error", "Entity does not exist: " + entityId);
			MessageProducer messageProducer = session.createProducer(message.getJMSReplyTo());
			messageProducer.send(outputMessage);
			return;
		}
		
		String text;
		if(message.getBooleanProperty(MessageFields.columnNamesOnly)) {
			text = source.getColumnNamesCsv();
		} else {			
			CsvLineCallbackStringWriter writer = writerThreadLocal.get();
			source.getAllCsvLines(writer);
			text = writer.toString();
		}
		
		TextMessage outputMessage = session.createTextMessage(text);
		outputMessage.setJMSCorrelationID(message.getJMSCorrelationID());
		
		MessageProducer messageProducer;
		try {
			messageProducer = session.createProducer(message.getJMSReplyTo());
		} catch(InvalidDestinationException ex) {
			log.warn("Cannot send reply; client has gone away: {}", message.getJMSReplyTo());
			return;
		}
		
		messageProducer.send(outputMessage);
		
		log.info("Completed query for entity {}", entityId);
	}
	
}
