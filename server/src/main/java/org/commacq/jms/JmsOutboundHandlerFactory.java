package org.commacq.jms;

import java.util.HashMap;
import java.util.Map;

import javax.jms.ConnectionFactory;

import org.commacq.layer.SubscribeLayer;

public class JmsOutboundHandlerFactory {
	
	public Map<String, JmsOutboundHandler> create(ConnectionFactory connectionFactory, SubscribeLayer layer, String broadcastTopicPattern) {
		Map<String, JmsOutboundHandler> handlers = new HashMap<String, JmsOutboundHandler>();
		for(String entityId : layer.getEntityIds()) {
			String broadcastTopic = String.format(broadcastTopicPattern, entityId);
			handlers.put(entityId, new JmsOutboundHandler(connectionFactory, layer, entityId, broadcastTopic));
		}
		return handlers;
	}
	
}
