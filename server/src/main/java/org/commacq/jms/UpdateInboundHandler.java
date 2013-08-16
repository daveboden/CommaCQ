package org.commacq.jms;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.lang3.Validate;
import org.commacq.DataManager;
import org.commacq.DataManager.UpdateCsvCacheResult;
import org.commacq.MessageFields;

@Slf4j
public class UpdateInboundHandler implements MessageListener {

    private final DataManager dataManager;

    public UpdateInboundHandler(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public void onMessage(Message message) {
        log.debug("Received update message: {}", message);
        if(message instanceof TextMessage) {
            onTextMessage((TextMessage) message);
        } else if(message instanceof MapMessage) {
            onMapMessage((MapMessage) message);
        }

    }

    //TODO - Doesn't have to be ids in the update message. Can be a group column.
    
    private void onTextMessage(TextMessage textMessage) {
        String entityId;
        String csvHeaderAndBody;
        try {
            entityId = textMessage.getStringProperty(MessageFields.entityId);
            csvHeaderAndBody = textMessage.getText();
        } catch (JMSException ex) {
            log.error("Could not perform update", ex);
            return;
        }
        
        log.info("Received an update for entity {}", entityId);
        
        
        String[][] csv = parseCsv(csvHeaderAndBody);
        List<String> ids = getIds(csv);
        
        UpdateCsvCacheResult result = dataManager.updateCsvCache(entityId, ids);
        
        // TODO Provide a hook in so that if users really want to read off the csv attibutes from the update manager
        // and stop the database manager going back to the database for the details then they can. Ill advised.
        // It may be better advised, though, to use one or two attributes on the update message to decide whether
        // or not a continuous query needs to hit the database.
    }
    
    /**
     * A map message update can update several entities' data in a single transaction.
     * Each map key specifies an entityId and the value is the update payload.
     * @param mapMessage
     */
    private void onMapMessage(MapMessage mapMessage) {
        List<String> entityIds;
        try {
            @SuppressWarnings("unchecked")
            Enumeration<String> mapNames = mapMessage.getMapNames();
            entityIds = Collections.list(mapNames);
        } catch (JMSException ex) {
            log.error("Could not perform update. Cannot access map message.");
            return;
        }
        
        log.info("Received an update for entities {}", entityIds);
        
        for(String entityId : entityIds) {
            String csvHeaderAndBody;
            try {
                csvHeaderAndBody = mapMessage.getString(entityId);
            } catch (JMSException ex) {
                log.error("Could not perform update for entity: {}", entityId);
                continue; // Try the next entityId in the message
            }
            String[][] csv = parseCsv(csvHeaderAndBody);
            List<String> ids = getIds(csv);
            
            UpdateCsvCacheResult result = dataManager.updateCsvCache(entityId, ids);
        }

    }
        
    private String[][] parseCsv(String csvHeaderAndBody) {
        Reader in = new StringReader(csvHeaderAndBody);
        String[][] csv;
        try {
            csv = new CSVParser(in).getAllValues();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        
        Validate.notEmpty(csv, "At least the header row is required in the CSV text");
        Validate.notEmpty(csv[0], "The CSV header row must contain at least one column");
        Validate.isTrue("id".equals(csv[0][0]), "The CSV header row's first column must be named id");
        
        return csv;
    }
    
    private List<String> getIds(String[][] csv) {        
        List<String> ids = new ArrayList<>();
        for(int i = 1; i < csv.length; i++) {
            ids.add(csv[i][0]);
        }
        return ids;
    }

}
