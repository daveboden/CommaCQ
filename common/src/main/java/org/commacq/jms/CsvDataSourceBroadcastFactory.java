package org.commacq.jms;

import java.util.ArrayList;
import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import lombok.RequiredArgsConstructor;

import org.commacq.CsvDataSource;
import org.commacq.CsvDataSourceFactory;
import org.commacq.CsvDataSourceLayer;
import org.commacq.CsvDataSourceLayerCollection;

/**
 * Starts listening for updates.
 * Creates a CsvDataSourceJmsQuery wired up to the update listener.
 */
@RequiredArgsConstructor
public class CsvDataSourceBroadcastFactory implements CsvDataSourceFactory {
	
	private final ConnectionFactory connectionFactory;
	private final String queryQueue;
	private final String broadcastTopicPattern;
	private final int timeoutInSeconds;

	@Override
	public CsvDataSourceJmsQuery createCsvDataSource(final String entityId) throws JMSException {
		final CsvDataSourceJmsQuery jmsQuery = new CsvDataSourceJmsQuery(entityId, connectionFactory, queryQueue);
		
		String broadcastTopic = String.format(broadcastTopicPattern, entityId);
		
		JmsBroadcastClient broadcastClient = new JmsBroadcastClient(entityId, jmsQuery, connectionFactory, broadcastTopic);
		broadcastClient.init();
		broadcastClient.start();
		
		return jmsQuery;
	}
	
	CsvDataSourceLayer create(CsvDataSourceLayer layer) throws JMSException {
		List<CsvDataSource> sources = new ArrayList<CsvDataSource>();
		for(String entityId : layer.getEntityIds()) {
			CsvDataSourceJmsQuery source = createCsvDataSource(entityId);
			sources.add(source);
		}
		CsvDataSourceLayer outputLayer = new CsvDataSourceLayerCollection(sources);
		return outputLayer;
	}
}
