package org.commacq.jms;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import lombok.extern.slf4j.Slf4j;

import org.commacq.CsvUpdatableDataSource;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.core.SessionCallback;
import org.springframework.jms.listener.SimpleMessageListenerContainer;


/**
 * In CQ mode, the server does all the hard work. This class creates a temporary queue
 * on which all updates will be received and then pushes a request onto the server's queue.
 * The server responds with a (potentially batched) initial load and then follows up
 * with incremental updates when required. This class maintains a continuous subscription
 * to the temporary queue. Even if the server restarts, the new instance of the server is
 * responsible for picking up where it left off and sending updates on the client's temporary
 * queue.
 */
@Slf4j
public class JmsCqClient<BeanType> {
	
	private final String entityId;
	private final CsvUpdatableDataSource csvUpdatableDataSource;
	
	SimpleMessageListenerContainer broadcastUpdateListener;
	
	public JmsCqClient(final String entityId, final CsvUpdatableDataSource csvUpdatableDataSource, ConnectionFactory connectionFactory, String fetchAllQueue, int timeoutInSeconds) throws JMSException {
		this.entityId = entityId;
		this.csvUpdatableDataSource = csvUpdatableDataSource;
		
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
		
		jmsTemplate.setReceiveTimeout(timeoutInSeconds);
		
		final Queue replyQueue = jmsTemplate.execute(new SessionCallback<Queue>() {
			@Override
			public Queue doInJms(Session session) throws JMSException {
				return session.createTemporaryQueue();
			}
		});
		
		jmsTemplate.send(fetchAllQueue, new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				TextMessage message = session.createTextMessage();
				message.setJMSReplyTo(replyQueue);
				return message;
			}
		});
		
		jmsTemplate.receive(replyQueue);
	}
	
	public void stop() {
		broadcastUpdateListener.stop();
	}
	
	public String getEntityId() {
		return entityId;
	}

}