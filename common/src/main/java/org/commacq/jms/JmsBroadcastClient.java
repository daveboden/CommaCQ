package org.commacq.jms;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.Validate;
import org.commacq.CsvTextBlockToCallback;
import org.commacq.layer.UpdatableLayer;
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
	private final UpdatableLayer csvUpdatableLayer;
	
	SimpleMessageListenerContainer broadcastUpdateListener;
	
	public JmsBroadcastClient(final String entityId, final UpdatableLayer csvUpdatableLayer,
			                  final ConnectionFactory connectionFactory, final String broadcastTopic) {
		this.entityId = entityId;
		this.csvUpdatableLayer = csvUpdatableLayer;
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
					String entityId = message.getStringProperty(MessageFields.entityId);
					Validate.notNull(entityId, "entityId must be present on incoming messages");
					String text = ((TextMessage)message).getText();
					
			    	csvTextBlockToCallback.presentTextBlockToCsvLineCallback(entityId, text, csvUpdatableLayer, true);
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