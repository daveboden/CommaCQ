package org.commacq.client.factory;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.commacq.client.BeanCacheUpdater;
import org.commacq.client.BeanCacheUpdaterJmsBroadcast;
import org.commacq.client.CsvToBeanStrategySpringConstructor;

public class BeanCacheUpdaterFactoryJmsBroadcast implements BeanCacheUpdaterFactory {

	private final BeanTypeSelectionStrategy beanTypeSelectionStrategy;
	private final ConnectionFactory connectionFactory;
	
	public BeanCacheUpdaterFactoryJmsBroadcast(BeanTypeSelectionStrategy beanTypeSelectionStrategy, ConnectionFactory connectionFactory) {
	    this.beanTypeSelectionStrategy = beanTypeSelectionStrategy;
	    this.connectionFactory = connectionFactory;
    }

	@Override
	public <BeanType> BeanCacheUpdater<BeanType> createBeanCacheUpdater(String entityId, Class<BeanType> beanType) {
		try {
            return new BeanCacheUpdaterJmsBroadcast<>(entityId, new CsvToBeanStrategySpringConstructor<>(beanType),
                connectionFactory, "query", "broadcast." + entityId, 20);
        } catch (JMSException ex) {
            throw new RuntimeException("Could not create JMS connection to for initial load and listening for broadcasts for entityId: " + entityId, ex);
        }
	}

	@Override
	public BeanCacheUpdater<?> createBeanCacheUpdater(String entityId) throws ClassNotFoundException {
		return createBeanCacheUpdater(entityId, beanTypeSelectionStrategy.chooseBeanType(entityId));
	}

}
