package org.commacq;

/**
 * Implementations of this interface are able to send a payload to a listening client.
 */
public interface OutboundHandler {

    void sendUpdate(final String entityId, final String payload);
    void sendBulkUpdate(final String entityId, final String payload); 
    
}