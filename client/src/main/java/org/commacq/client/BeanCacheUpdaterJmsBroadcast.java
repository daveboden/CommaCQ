package org.commacq.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.core.SessionCallback;
import org.springframework.jms.listener.SimpleMessageListenerContainer;

import org.commacq.MessageFields;


/**
 * First connects to the broadcast topic and relies on the Jms server to store up messages.
 * 
 * Then uses Jms to go and get the initial load (if we're using Jms to get the updates
 * then why not use Jms to get the initial load too? No significant advantage to using
 * HTTP apart from probably unnecessarily reducing load on the Jms server).
 * 
 * Once the initial load is successfully completed, merges the broadcast updates
 * in and then continues to monitor the broadcast topic.
 */
public class BeanCacheUpdaterJmsBroadcast<BeanType> extends BeanCacheUpdaterBase<BeanType> {

	private static Logger logger = LoggerFactory.getLogger(BeanCacheUpdaterJmsBroadcast.class);
	
	
	private final String entityId;
	private final ConcurrentHashMap<String, BeanType> beanCache;
		
	//Destination info is maintained in case we want to reinitialise the whole cache
	private final CsvToBeanStrategy<BeanType> csvToBeanStrategy;
	
	SimpleMessageListenerContainer broadcastUpdateListener;
	
	public BeanCacheUpdaterJmsBroadcast(final String entityId, final CsvToBeanStrategy<BeanType> csvToBeanStrategy, ConnectionFactory connectionFactory, String initialLoadQueue, String broadcastTopic, int timeoutInSeconds) throws JMSException {
		this.entityId = entityId;
		this.csvToBeanStrategy = csvToBeanStrategy;
		
		broadcastUpdateListener = new SimpleMessageListenerContainer();
		broadcastUpdateListener.setConnectionFactory(connectionFactory);
		broadcastUpdateListener.setPubSubDomain(true);
		broadcastUpdateListener.setDestinationName(broadcastTopic);
		
		logger.info("Initializing the update subscription for entity {} and leaving the messages on the topic for now", entityId);
		broadcastUpdateListener.initialize();
		
		beanCache = new ConcurrentHashMap<>();
		
		JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
		
		/*
		 * Wait, for example, 30 seconds for the reply to come back from the server.
		 * This is mainly to account for the situation where all processes in an
		 * entire system may be started at the same time. Without a timeout and
		 * the persistence of the middleware queue, the system owner would need to
		 * ensure that the Comma CQ server was started before all other components
		 * in the system. Middleware removes this requirement by storing up requests
		 * for data from other components until the Comma CQ server is ready to service
		 * them.
		 */
		jmsTemplate.setReceiveTimeout(timeoutInSeconds * 1000);
		
		final Queue replyQueue = jmsTemplate.execute(new SessionCallback<Queue>() {
			@Override
			public Queue doInJms(Session session) throws JMSException {
				return session.createTemporaryQueue();
			}
		});
		
		jmsTemplate.send(initialLoadQueue, new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				TextMessage message = session.createTextMessage();
				message.setJMSReplyTo(replyQueue);
				message.setStringProperty(MessageFields.entityId, entityId);
				return message;
			}
		});
		
		TextMessage initialLoad = (TextMessage)jmsTemplate.receive(replyQueue);
		if(initialLoad == null) {
			throw new RuntimeException("No data returned from server (timed out): " + entityId);
		}
		String error = initialLoad.getStringProperty("error");
		if(error != null) {
			throw new RuntimeException("Error returned from server: " + error);
		}
		
		Map<String, BeanType> beans = csvToBeanStrategy.getBeans(initialLoad.getText()).getUpdated();
		
		beanCache.putAll(beans);
		
		logger.info("Starting the update subscription for entity {}", entityId);
		broadcastUpdateListener.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(Message message) {
				try {
					CsvToBeanStrategyResult<BeanType> result = csvToBeanStrategy.getBeans(((TextMessage)message).getText());
					beanCache.putAll(result.getUpdated());
					for(String deleted : result.getDeleted()) {
						beanCache.remove(deleted);
					}
					notifyCacheObservers(result.getUpdated(), result.getDeleted());
				} catch (JMSException ex) {
					throw new RuntimeException(ex);
				}
			}
		});
		broadcastUpdateListener.start();
		//TODO ideally we should wait until all the built up messages on the broadcast
		//topic are consumed.
	}
	
	@Override
	public ConcurrentHashMap<String, BeanType> getInitializedBeanCache() {
		return beanCache;
	}
	
	public void stop() {
		broadcastUpdateListener.stop();
	}
	
	@Override
	public String getEntityId() {
		return entityId;
	}
	
	@Override
	public Class<BeanType> getBeanType() {
		return csvToBeanStrategy.getBeanType();
	}

}