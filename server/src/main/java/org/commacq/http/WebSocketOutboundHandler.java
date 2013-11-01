package org.commacq.http;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.extern.slf4j.Slf4j;

import org.commacq.OutboundHandler;
import org.eclipse.jetty.websocket.WebSocket.Connection;

@Slf4j
public class WebSocketOutboundHandler implements OutboundHandler {

    private final List<Connection> connections = new CopyOnWriteArrayList<>();
    
    public void addConnection(Connection connection) {
        connections.add(connection);
    }
    
    public void removeConnection(Connection connection) {
        connections.remove(connection);
    }
    
    @Override
    public void sendUpdate(String entityId, String payload) {
        for(Connection connection : connections) {
            try {
                connection.sendMessage(payload);
            } catch (IOException ex) {
                removeConnectionAfterException(connection);
            }
        }
        log.info("Pushed updates to {} clients", connections.size());
    }
    
    @Override
    public void sendBulkUpdate(String entityId, String payload) {
        for(Connection connection : connections) {
            try {
                connection.sendMessage(payload);
            } catch (IOException ex) {
                removeConnectionAfterException(connection);
            }
        }
        log.info("Pushed bulk updates to {} clients", connections.size());        
    }
    
    private void removeConnectionAfterException(Connection connection) {
        log.info("Client has gone without removing itself cleanly: {}", connection);
        connections.remove(connection);
    }
    
}