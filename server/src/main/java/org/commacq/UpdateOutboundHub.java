package org.commacq;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

import org.commacq.DataManager.UpdateCsvCacheResult;

public class UpdateOutboundHub {
    
    final List<OutboundHandler> outboundHandlers;
    
    public UpdateOutboundHub(List<OutboundHandler> outboundHandlers) {
        this.outboundHandlers = Collections.unmodifiableList(outboundHandlers);
    }
	
	public void sendUpdate(final String entityId, final CsvCache csvCache, final UpdateCsvCacheResult result) {
	    
	    String payload = getPayload(csvCache, result);
        
        for(OutboundHandler handler : outboundHandlers) {
            handler.sendUpdate(entityId, payload);
        }
	}
	
	public void sendBulkUpdate(final String entityId, final CsvCache csvCache) { 
	    
	}
	
	String getPayload(final CsvCache csvCache, final UpdateCsvCacheResult result) {
	    StringWriter writer = new StringWriter();
        
        try {
            csvCache.writeToOutput(writer, result.getUpdatedIds());
            
            //Deletions are represented by a line containing only an id
            for(String id : result.getDeletedIds()) {
                writer.append("\r\n");
                writer.append(id);
            }
            
        } catch (IOException ex) {
            //Can't see how this would happen with a StringWriter
            throw new RuntimeException(ex);
        }
        
        String payload = writer.toString();
        
        return payload;
	}
}