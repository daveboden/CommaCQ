package org.commacq.jms;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import lombok.extern.slf4j.Slf4j;

import org.commacq.BlockCallback;
import org.commacq.CsvDataSource;
import org.commacq.CsvLine;
import org.commacq.CsvLineCallbackListImpl;
import org.commacq.CsvUpdateBlockException;
import org.commacq.LineCallback;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.core.SessionCallback;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

@Slf4j
public class CsvDataSourceJmsQuery implements CsvDataSource {

	private final String entityId;
	private final ConnectionFactory connectionFactory;
	private final String queryQueue;
	private final JmsTemplate jmsTemplate;
	private final Queue replyQueue;
	
	public CsvDataSourceJmsQuery(String entityId, ConnectionFactory connectionFactory, String initialLoadQueue) {
		this.entityId = entityId;
		this.connectionFactory = connectionFactory;
		this.queryQueue = initialLoadQueue;
		
		jmsTemplate = new JmsTemplate(connectionFactory);
		
		replyQueue = jmsTemplate.execute(new SessionCallback<Queue>() {
			@Override
			public Queue doInJms(Session session) throws JMSException {
				return session.createTemporaryQueue();
			}
		});
	}
	
	public int getTimeoutInSeconds() {
		return (int)(jmsTemplate.getReceiveTimeout() / 1000);
	}
	
	/**
	 * Wait, for example, 30 seconds for the reply to come back from the server.
	 * This is mainly to account for the situation where all processes in an
	 * entire system may be started at the same time. Without a timeout and
	 * the persistence of the middleware queue, the system owner would need to
	 * ensure that the Comma CQ server was started before all other components
	 * in the system. Middleware removes this requirement by storing up requests
	 * for data from other components until the Comma CQ server is ready to service
	 * them.
	 */
	public void setTimeoutInSeconds(int timeoutInSeconds) {		
		jmsTemplate.setReceiveTimeout(timeoutInSeconds * 1000);
	}

	@Override
	public String getEntityId() {
		return entityId;
	}

	@Override
	public void getAllCsvLines(final LineCallback callback) {
		//TODO Replace these nulls
		makeRequest(callback, null);
	}

	@Override
	public void getCsvLines(final Collection<String> ids, final LineCallback callback) {
		makeRequest(callback, new MessageSetter() {
			@Override
			public void addProperties(TextMessage message) throws JMSException {
				StringBuilder builder = new StringBuilder();
				for(String id : ids) {
					builder.append(id).append("\n");
				}
				message.setStringProperty(MessageFields.ids, builder.toString());
			}
		});
	}

	@Override
	public void getCsvLine(final String id, final LineCallback callback) {
		makeRequest(callback, new MessageSetter() {
			@Override
			public void addProperties(TextMessage message) throws JMSException {
				message.setStringProperty(MessageFields.id, id);
			}
		});
	}

	@Override
	public void getCsvLinesForGroup(final String group, final String idWithinGroup, final LineCallback callback) {
		makeRequest(callback, new MessageSetter() {
			@Override
			public void addProperties(TextMessage message) throws JMSException {
				message.setStringProperty(MessageFields.group, group);
				message.setStringProperty(MessageFields.idWithinGroup, idWithinGroup);				
			}
		});
	}
	
	@Override
	public String getColumnNamesCsv() {
		CsvLineCallbackListImpl listCallback = new CsvLineCallbackListImpl();
		makeRequest(listCallback, new MessageSetter() {
			@Override
			public void addProperties(TextMessage message) throws JMSException {
				message.setBooleanProperty(MessageFields.columnNamesOnly, true);
			}
		});
		
		return listCallback.getColumnNamesCsv();
	}
	
	private void makeRequest(final LineCallback callback, final MessageSetter messageSetter) {
		jmsTemplate.send(queryQueue, new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				TextMessage message = session.createTextMessage();
				message.setJMSReplyTo(replyQueue);
				message.setStringProperty(MessageFields.entityId, entityId);
				if(messageSetter != null) {
					messageSetter.addProperties(message);
				}
				return message;
			}
		});
		
		TextMessage initialLoad = (TextMessage)jmsTemplate.receive(replyQueue);
		if(initialLoad == null) {
			throw new RuntimeException("No data returned from server (timed out): " + entityId);
		}
		String error;
		try {
			error = initialLoad.getStringProperty("error");
		} catch (JMSException ex) {
			throw new RuntimeException("Could not read error property", ex);
		}
		if(error != null) {
			throw new RuntimeException("Error returned from server: " + error);
		}
		
		String text;
		try {
			text = initialLoad.getText();
		} catch (JMSException ex) {
			throw new RuntimeException("Could not get text from message", ex);
		}
		
		processResult(callback, text);
	}
	
	private interface MessageSetter {
		void addProperties(TextMessage message) throws JMSException;
	}
	
	private void processResult(LineCallback callback, String text) {
		CsvListReader parser = new CsvListReader(new StringReader(text), CsvPreference.STANDARD_PREFERENCE);
		
		try {
		
			parser.getHeader(true);
			String columnNamesCsv = parser.getUntokenizedRow();
			
			List<String> body;
			
			while((body = parser.read()) != null) {
				String line = parser.getUntokenizedRow();
				CsvLine csvLine = new CsvLine(body.get(0), line);
				callback.processUpdate(entityId, columnNamesCsv, csvLine);
			}
			
		} catch(CsvUpdateBlockException ex) {
			throw new RuntimeException(ex);
		} catch(IOException ex) {
			throw new RuntimeException("Error parsing CSV", ex);
		} finally {
			try {
				parser.close();
			} catch (IOException ex) {
				log.warn("Error closing CSV parser", ex);
			}
		}
	}
	
}
