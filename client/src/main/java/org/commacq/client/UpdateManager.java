package org.commacq.client;

import java.util.Arrays;
import java.util.Collection;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.lang3.Validate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.commacq.jms.MessageFields;

public class UpdateManager {
	
	private final JmsTemplate jmsTemplate;
	
	public UpdateManager(ConnectionFactory connectionFactory, String updateQueue) {
		Validate.notNull(updateQueue, "updateQueue must be specified");

		jmsTemplate = new JmsTemplate(connectionFactory);
		jmsTemplate.setDefaultDestinationName(updateQueue);
	}
	
	public void sendUpdate(final String entityId, final String id) {
		jmsTemplate.send(new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				TextMessage textMessage = session.createTextMessage();
				textMessage.setStringProperty(MessageFields.entityId, entityId);
				textMessage.setText("id\n" + id);
				return textMessage;
			}
		});
	}
	
	public void sendUpdate(final String entityId, final Collection<String> ids) {
		jmsTemplate.send(new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				TextMessage textMessage = session.createTextMessage();
				textMessage.setStringProperty(MessageFields.entityId, entityId);
				StringBuilder text = new StringBuilder("id");
				for(String id : ids) {
					text.append('\n').append(id);
				}
				textMessage.setText(text.toString());
				return textMessage;
			}
		});
	}
	
	public void sendUpdate(final String entityId, final String... ids) {
		sendUpdate(entityId, Arrays.asList(ids));
	}
	
}