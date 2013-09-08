package org.commacq.jms;

import static org.commacq.CsvUpdatableDataSource.UpdateMode.untrusted;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import lombok.extern.slf4j.Slf4j;

import org.commacq.CsvTextBlockToCallback;
import org.commacq.CsvUpdatableDataSource;
import org.commacq.CsvUpdatableDataSource.UpdateMode;
import org.springframework.jms.listener.SimpleMessageListenerContainer;


/**
 * Connects to the broadcast topic and relies on the Jms server to store up messages.
 * 
 * Once the initial load is successfully completed, merges the broadcast updates
 * in and then continues to monitor the broadcast topic.
 * 
 * Encapsulates the format of the incoming message, where an id with no additional
 * details and no trailing commas means a delete.
 */
@Slf4j
public class JmsBroadcastClient {
	
	private final String entityId;
	private final ConnectionFactory connectionFactory;
	private final String broadcastTopic;
	private final CsvTextBlockToCallback csvTextBlockToCallback = new CsvTextBlockToCallback();
	private final CsvUpdatableDataSource csvUpdatableDataSource;
	private final UpdateMode updateMode = untrusted;
	
	SimpleMessageListenerContainer broadcastUpdateListener;
	
	public JmsBroadcastClient(final String entityId, final CsvUpdatableDataSource csvUpdatableDataSource,
			                  final ConnectionFactory connectionFactory, final String broadcastTopic) {
		this.entityId = entityId;
		this.csvUpdatableDataSource = csvUpdatableDataSource;
		this.connectionFactory = connectionFactory;
		this.broadcastTopic = broadcastTopic;
	}
	
	public void init() throws JMSException {
		broadcastUpdateListener = new SimpleMessageListenerContainer();
		broadcastUpdateListener.setConnectionFactory(connectionFactory);
		broadcastUpdateListener.setPubSubDomain(true);
		broadcastUpdateListener.setDestinationName(broadcastTopic);
		
		log.info("Initializing the update subscription for entity {} on topic {} and leaving the messages on the topic for now", entityId, broadcastTopic);
		broadcastUpdateListener.initialize();
	}
	
	public void start() throws JMSException {
		log.info("Starting the update subscription for entity {}", entityId);
		broadcastUpdateListener.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(Message message) {
				log.info("Received message on topic {}", broadcastTopic);
				try {
					String text = ((TextMessage)message).getText();
					
			    	csvTextBlockToCallback.presentTextBlockToCsvLineCallback(text, csvUpdatableDataSource, true);
				} catch (JMSException ex) {
					throw new RuntimeException(ex);
				}
			}
		});
		broadcastUpdateListener.start();
		//TODO ideally we should wait until all the built up messages on the broadcast
		//topic are consumed.
	}
	
	public void stop() {
		broadcastUpdateListener.stop();
	}
	
	public String getEntityId() {
		return entityId;
	}

}