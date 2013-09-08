package org.commacq.jms;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import lombok.extern.slf4j.Slf4j;

import org.commacq.CsvDataSource;
import org.commacq.CsvLine;
import org.commacq.CsvLineCallback;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**
 * Maintains the outbound broadcast topics and organises the sending of broadcast updates.
 */
@Slf4j
public class JmsOutboundHandler implements CsvLineCallback {

    private final JmsTemplate jmsTemplate;
    private final CsvDataSource csvDataSource;
    private final String broadcastTopic;
    
    private final StringWriter currentText = new StringWriter();
    private final PrintWriter currentWriter = new PrintWriter(currentText);
    private boolean bulkUpdate = false;

    public JmsOutboundHandler(ConnectionFactory connectionFactory, CsvDataSource csvDataSource, String broadcastTopic) {
        jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setPubSubDomain(true);
        
        this.csvDataSource = csvDataSource;
        
        this.broadcastTopic = broadcastTopic;
        
        //use of "this" should be the last line in the constructor
        csvDataSource.subscribe(this);
    }
    
    @Override
    public void startUpdateBlock(String columnNamesCsv) {
    	currentWriter.println(columnNamesCsv);
    }
    
    @Override
    public void finishUpdateBlock() {    	
        jmsTemplate.send(broadcastTopic, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage textMessage = session.createTextMessage();
                textMessage.setStringProperty(MessageFields.entityId, csvDataSource.getEntityId());
                textMessage.setBooleanProperty(MessageFields.bulkUpdate, bulkUpdate);
                
                String payload = currentText.toString();
                
                textMessage.setText(payload);
                
                log.info("Sending message to topic {}", broadcastTopic);
                
                return textMessage;
            }
        });
    	
    	//Reset the buffer
    	currentText.getBuffer().setLength(0);
    	bulkUpdate = false;
    }
    
    @Override
    public void processUpdate(String columnNamesCsv, CsvLine csvLine) {
    	currentWriter.println(csvLine.getCsvLine());
    }
    
    @Override
    public void processRemove(String id) {
    	currentWriter.println(id);
    }
    
    @Override
    public void startBulkUpdate(String columnNamesCsv) {
    	bulkUpdate = true;
    }
    
    @Override
    public void startBulkUpdateForGroup(String group, String idWithinGroup) {
    	//Not yet supported
    }
    
}
