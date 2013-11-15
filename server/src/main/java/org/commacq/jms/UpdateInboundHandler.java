package org.commacq.jms;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import lombok.extern.slf4j.Slf4j;

import org.commacq.CsvTextBlockToCallback;
import org.commacq.CsvUpdateBlockException;
import org.commacq.layer.UpdatableLayer;
import org.commacq.layer.Layer;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

/**
 * A single listener processes updates, directing them to the relevant
 * CsvDataSource for the entity.
 * 
 * If updates came on different channels for each entity, there would be
 * no chance of coordinating updates transactionally across entities.
 * 
 * Updates that arrive with just the id column specified are by default
 * treaded as untrusted. Updates arrive with many columns specified are
 * by default trusted.
 */
@Slf4j
public class UpdateInboundHandler implements MessageListener {
	
	private final Layer layer;
	private final CsvTextBlockToCallback csvTextBlockToCallback = new CsvTextBlockToCallback();

    public UpdateInboundHandler(Layer layer) {
        this.layer = layer;
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
        
        
        handlePayload(entityId, csvHeaderAndBody);
        
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
            
            
            handlePayload(entityId, csvHeaderAndBody);
        }

    }
    
    private void handlePayload(final String entityId, final String csvHeaderAndBody) {
    	CsvListReader parser = new CsvListReader(new StringReader(csvHeaderAndBody), CsvPreference.STANDARD_PREFERENCE);
    	try {
	    	String[] header = parser.getHeader(true);
	    	
	    	UpdatableLayer source = (UpdatableLayer)layer.getCsvDataSource(entityId);
	    	
	    	if(header.length == 1 && header[0].equals("id")) {
	    		log.info("Update for entity {} contains a list of ids", entityId);
	    		List<String> line;
	    		try {
	    			source.startUpdateBlock(entityId, source.getCsvDataSource(entityId).getColumnNamesCsv());
					while((line = parser.read()) != null) {
						source.updateUntrusted(entityId, line.get(0));
					}
					source.finish();
				} catch(CsvUpdateBlockException ex) {
					throw new RuntimeException(ex);
				}
	    		return;
	    	}
	    	log.info("Update for entity {} contains ids and column headings", entityId);
	    	
	    	csvTextBlockToCallback.presentTextBlockToCsvLineCallback(entityId, csvHeaderAndBody, source, true);
    	} catch (IOException ex) {				
			throw new RuntimeException("Couldn't parse CSV", ex);
    	} finally {
			try {
				parser.close();
			} catch (IOException ex) {
				log.warn("Error closing CSV parser", ex);
			}
    	}
    	
    }
    
    

}
