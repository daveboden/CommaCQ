package org.commacq.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.commacq.CsvDataSource;
import org.commacq.CsvDataSourceLayer;
import org.commacq.CsvLineCallbackStringWriter;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;

//TODO thread safety
@Slf4j
public class CsvWebSocketHandler extends WebSocketHandler {

	private ThreadLocal<CsvLineCallbackStringWriter> writerThreadLocal = new ThreadLocal<CsvLineCallbackStringWriter>() {
		protected CsvLineCallbackStringWriter initialValue() {
			return new CsvLineCallbackStringWriter();
		};
		
		public CsvLineCallbackStringWriter get() {
			CsvLineCallbackStringWriter writer = super.get();
			writer.clear();
			return writer;
		};
	};
	
	private CsvDataSourceLayer layer;
	
	private List<PublishWebSocket> websockets = new ArrayList<>();
	
	private final WebSocketOutboundHandler webSocketOutboundHandler;
	
	public CsvWebSocketHandler(CsvDataSourceLayer layer, WebSocketOutboundHandler webSocketOutboundHandler) {
		this.layer = layer;
		this.webSocketOutboundHandler = webSocketOutboundHandler;
	}

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
		String entityId = HttpUtils.getEntityStringFromTarget(request.getRequestURL().toString());
		log.info("Creating web socket for entity: {}", entityId);
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
				CsvDataSource source = layer.getCsvDataSource(entityId);
				if(source == null) {
				    String message = String.format("Unknown entity id: %s", entityId);
				    log.warn(message);
				    connection.sendMessage(message);
				    return;
				}
				CsvLineCallbackStringWriter writer = writerThreadLocal.get();
				source.getAllCsvLines(writer);
				connection.sendMessage(writer.toString());
				log.info("Pushed {} entities to client", writer.getProcessUpdateCount());
				webSocketOutboundHandler.addConnection(connection);
			} catch (IOException ex) {
				log.warn("Could not send message to opened connection", ex);
			}
		};
		@Override
		public void onClose(int closeCode, String message) {
			websockets.remove(this);
			webSocketOutboundHandler.removeConnection(connection);
		}
	}
	
}