package org.commacq;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**
 * Only used in high-availability mode. Registered as a listener on the primary
 * update queue.
 * 
 * It must be registered on the same {@link Connection} as the {@link UpdateInboundHandler}
 * because this class handles the update as well as doing the republishing. The mechanism
 * relies on the "noLocal" flag to tell the middleware not to deliver the republish message
 * back to the same server on the topic listener. In this way, the update message won't get
 * handled twice by the high-availabilty server that initially received the update.
 * 
 * The transaction management behaviour should be that all the message republishes and the
 * consumption of the incoming update message should be committed together. The other
 * high-availability servers should only hear about the update on the republish topic
 * once it has been successfully processed by the server that initially picked up the message.
 */
public class UpdateInboundHandlerRepublishHighAvailability extends UpdateInboundHandler {

	private static Logger logger = LoggerFactory.getLogger(UpdateInboundHandlerRepublishHighAvailability.class);

	private final String republishTopic;
	private final JmsTemplate jmsTemplate;
	
	public UpdateInboundHandlerRepublishHighAvailability(DataManager dataManager,
			                                             ConnectionFactory connectionFactory, String republishTopic) {
		super(dataManager);
		this.republishTopic = republishTopic;
		this.jmsTemplate = new JmsTemplate(connectionFactory);
	}
	
	@Override
	public void onMessage(final Message message) {
		logger.debug("Republishing message to high availability topic");		
		jmsTemplate.send(republishTopic, new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				return message;
			}
		});
		
		super.onMessage(message);
	}
	
}
