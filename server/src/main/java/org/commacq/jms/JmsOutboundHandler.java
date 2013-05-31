package org.commacq.jms;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import org.commacq.MessageFields;
import org.commacq.OutboundHandler;

/**
 * Maintains the outbound broadcast topics and organises the sending of broadcast updates.
 * For CQ mode, maintains the list of client subscriptions.
 * 
 * Only sends messages; is not responsible for receiving notifications of updates.
 */
public class JmsOutboundHandler implements OutboundHandler {

    private final JmsTemplate jmsTemplate;

    public JmsOutboundHandler(ConnectionFactory connectionFactory) {
        jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setPubSubDomain(true);
    }
    
    @Override
    public void sendUpdate(final String entityId, final String payload) {
        jmsTemplate.send(getBroadcastTopic(entityId), new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage textMessage = session.createTextMessage();
                textMessage.setStringProperty(MessageFields.entityId, entityId);
                
                textMessage.setText(payload);
                
                return textMessage;
            }
        });
    }
    
    @Override
    public void sendBulkUpdate(final String entityId, final String payload) {
        jmsTemplate.send(getBroadcastTopic(entityId), new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage textMessage = session.createTextMessage();
                textMessage.setStringProperty(MessageFields.entityId, entityId);
                textMessage.setBooleanProperty(MessageFields.bulkUpdate, true);
                
                textMessage.setText(payload);
                
                return textMessage;
            }
        });
    }
    
    String getBroadcastTopic(String entityId) {
        return "broadcast." + entityId;
    }
    
    
}