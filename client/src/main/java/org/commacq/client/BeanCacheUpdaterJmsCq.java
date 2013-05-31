package org.commacq.client;

import java.util.concurrent.ConcurrentHashMap;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.core.SessionCallback;


/**
 * In CQ mode, the server does all the hard work. This class creates a temporary queue
 * on which all updates will be received and then pushes a request onto the server's queue.
 * The server responds with a (potentially batched) initial load and then follows up
 * with incremental updates when required. This class maintains a continuous subscription
 * to the temporary queue. Even if the server restarts, the new instance of the server is
 * responsible for picking up where it left off and sending updates on the client's temporary
 * queue.
 */
class BeanCacheUpdaterJmsCq<BeanType> extends BeanCacheUpdaterBase<BeanType> {

	private final String entityId;
	private final ConcurrentHashMap<String, BeanType> beanCache;
	private final Queue fetchAll;
	private final JmsTemplate jmsTemplate;
	
	public BeanCacheUpdaterJmsCq(String entityId, Queue fetchAll, int timeoutInSeconds) {
		this.entityId = entityId;
		this.fetchAll = fetchAll;
		
		//TODO change to using a camel route to get hold of the data over JMS.
		
		jmsTemplate = new JmsTemplate();
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
		jmsTemplate.setReceiveTimeout(timeoutInSeconds);
		
		final Queue replyQueue = jmsTemplate.execute(new SessionCallback<Queue>() {
			@Override
			public Queue doInJms(Session session) throws JMSException {
				return session.createTemporaryQueue();
			}
		});
		
		jmsTemplate.send(fetchAll, new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				TextMessage message = session.createTextMessage();
				message.setJMSReplyTo(replyQueue);
				return message;
			}
		});
		
		jmsTemplate.receive(replyQueue);
		
		beanCache = new ConcurrentHashMap<>();
	}
	
	@Override
	public ConcurrentHashMap<String, BeanType> getInitializedBeanCache() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getEntityId() {
		return entityId;
	}
	
	@Override
	public Class<BeanType> getBeanType() {
		return null;
	}
	
}