package org.commacq.jms;

import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import lombok.extern.slf4j.Slf4j;

import org.commacq.CsvDataSource;
import org.commacq.CsvLineCallbackStringWriter;
import org.commacq.layer.Layer;
import org.springframework.jms.listener.SessionAwareMessageListener;

/**
 * Handles inbound requests for initial data loads. Examines whether the client is subscribed
 * for broadcast or requires a more complex continuous query (CQ). Delivers the required data
 * to the client and, if a CQ, sets up the record of the query so that updates will be handled
 * and delivered to the client.
 * 
 * Incoming messages always have a reply queue set (normally a temporary queue).
 * 
 * QueryInboundHandler exposes a layer.
 * Clients can demand individual entities by name, or can ask the handler for all the entity identifiers that
 * the layer contains and then get them all. To request the list of entityIds, the command is "listEntityIds".
 * The request message from the client contains an entityId property to specify which entity is required.
 * It supports a "columnNamesOnly" attribute if the client just wants to get hold of the header information.
 */
@Slf4j
public class QueryInboundHandler implements SessionAwareMessageListener<Message> {
	
	private final Layer layer;
	
	public QueryInboundHandler(Layer layer) {
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
		
		String command = message.getStringProperty(MessageFields.command);
		if(MessageFields.command_listEntityIds.equals(command)) {
			log.info("Received a query for the list of entityIds");
			MessageProducer messageProducer = session.createProducer(message.getJMSReplyTo());
			TextMessage outputMessage = session.createTextMessage();
			StringBuilder stringBuilder = new StringBuilder();
			for(String entityId : layer.getEntityIds()) {
				stringBuilder.append(entityId).append("\r\n");
			}
			outputMessage.setText(stringBuilder.toString());
			messageProducer.send(outputMessage);
			return;
		}
		
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
