package org.commacq.http;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.commacq.CsvCache;
import org.commacq.DataManager;

//TODO thread safety
public class CsvWebSocketHandler extends WebSocketHandler {

	private static final Logger logger = LoggerFactory.getLogger(CsvWebSocketHandler.class);
	
	private DataManager dataManager;
	
	private List<PublishWebSocket> websockets = new ArrayList<>();
	
	private final WebSocketOutboundHandler webSocketOutboundHandler;
	
	public CsvWebSocketHandler(DataManager dataManager, WebSocketOutboundHandler webSocketOutboundHandler) {
		this.dataManager = dataManager;
		this.webSocketOutboundHandler = webSocketOutboundHandler;
	}

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
		String entityId = HttpUtils.getEntityStringFromTarget(request.getRequestURL().toString());
		logger.info("Creating web socket for entity: {}", entityId);
		PublishWebSocket webSocket = new PublishWebSocket(entityId);
		websockets.add(webSocket);
		return webSocket;
	}
	
	class PublishWebSocket implements WebSocket {
		
		private Connection connection;
		private String entityId;
		
		PublishWebSocket(String entityId) {
			this.entityId = entityId;
		}
		
		@Override
		public void onOpen(Connection connection) {
			this.connection = connection;
			try {
				CsvCache csvCache = dataManager.getCsvCache(entityId);
				if(csvCache == null) {
				    String message = String.format("Unknown entity id: %s", entityId);
				    logger.warn(message);
				    connection.sendMessage(message);
				    return;
				}
				StringWriter sw = new StringWriter();
				csvCache.writeToOutput(sw);
				connection.sendMessage(sw.toString());
				logger.info("Pushed {} entities to client", csvCache.size());
				webSocketOutboundHandler.addConnection(connection);
			} catch (IOException ex) {
				logger.warn("Could not send message to opened connection", ex);
			}
		};
		@Override
		public void onClose(int closeCode, String message) {
			websockets.remove(this);
			webSocketOutboundHandler.removeConnection(connection);
		}
	}
	
}