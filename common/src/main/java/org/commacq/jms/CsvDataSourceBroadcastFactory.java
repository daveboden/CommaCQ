package org.commacq.jms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.StringUtils;
import org.commacq.CsvDataSource;
import org.commacq.layer.DataSourceCollectionLayer;
import org.commacq.layer.Layer;
import org.commacq.layer.UpdatableLayer;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.core.SessionCallback;

/**
 * Starts listening for updates.
 * Creates a CsvDataSourceJmsQuery wired up to the update listener.
 */
@RequiredArgsConstructor
public class CsvDataSourceBroadcastFactory {
	
	private final ConnectionFactory connectionFactory;
	private final String queryQueue;
	private final String broadcastTopicPattern;
	private final int timeoutInSeconds;
	
	/**
	 * Creates a proxy layer by requesting all entity ids from the upstream server
	 * and creating a data source for each entity.
	 * 
	 * @param layer
	 * @return
	 * @throws JMSException
	 */
	Layer createLayer() throws JMSException {
		JmsTemplate template = new JmsTemplate(connectionFactory);
		template.setReceiveTimeout(timeoutInSeconds * 1000);
		
		final TemporaryQueue replyQueue = template.execute(new SessionCallback<TemporaryQueue>() {
			@Override
			public TemporaryQueue doInJms(Session session) throws JMSException {
				return session.createTemporaryQueue();
			}
		});
		
		template.send(queryQueue, new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				TextMessage textMessage = session.createTextMessage();
				textMessage.setJMSReplyTo(replyQueue);
				textMessage.setStringProperty(MessageFields.command, MessageFields.command_listEntityIds);
				return textMessage;
			}
		});
		
		TextMessage replyMessage = (TextMessage)template.receive(replyQueue);
		
		if(replyMessage == null) {
			throw new RuntimeException("No response from server on: " + queryQueue);
		}
		
		String entityIdsText = replyMessage.getText();
		
		BufferedReader reader = new BufferedReader(new StringReader(entityIdsText));
		
		SortedSet<String> entityIds = new TreeSet<>();
		String line;
		try {
			while((line = reader.readLine()) != null) {
				if(StringUtils.isBlank(line)) {
					continue;
				}
				
				entityIds.add(line);
			}
		} catch (IOException ex) {
			throw new RuntimeException("IOException not expected to happen with StringReader", ex);
		}
		
		List<CsvDataSource> sources = new ArrayList<CsvDataSource>();
		for(String entityId : entityIds) {
			final CsvDataSourceJmsQuery jmsQuery = new CsvDataSourceJmsQuery(entityId, connectionFactory, queryQueue);
			sources.add(jmsQuery);			
		}
		
		UpdatableLayer outputLayer = new DataSourceCollectionLayer(sources);
		
		for(String entityId : entityIds) {
			createBroadcastListener(entityId, outputLayer);
		}
		
		return outputLayer;
	}
	
	private void createBroadcastListener(final String entityId, final UpdatableLayer layer) throws JMSException {
		String broadcastTopic = String.format(broadcastTopicPattern, entityId);
		
		JmsBroadcastClient broadcastClient = new JmsBroadcastClient(entityId, layer, connectionFactory, broadcastTopic);
		broadcastClient.init();
		broadcastClient.start();
		
		return ;
	}
}
