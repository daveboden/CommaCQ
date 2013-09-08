package org.commacq.jms;

import java.util.HashMap;
import java.util.Map;

import javax.jms.ConnectionFactory;

import org.commacq.CsvDataSource;
import org.commacq.CsvDataSourceLayer;

public class JmsOutboundHandlerFactory {
	
	public Map<String, JmsOutboundHandler> create(ConnectionFactory connectionFactory, CsvDataSourceLayer layer, String broadcastTopicPattern) {
		Map<String, JmsOutboundHandler> handlers = new HashMap<String, JmsOutboundHandler>();
		for(CsvDataSource source : layer.getMap().values()) {
			String broadcastTopic = String.format(broadcastTopicPattern, source.getEntityId());
			handlers.put(source.getEntityId(), new JmsOutboundHandler(connectionFactory, source, broadcastTopic));
		}
		return handlers;
	}
	
}
