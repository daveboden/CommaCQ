package org.commacq.client.factory;

import org.commacq.client.BeanCacheUpdater;
import org.commacq.client.BeanCacheUpdaterNoFetch;

/**
 * An implementation of a BeanCacheUpdaterFactory that does nothing except
 * for decide on which beanType should be used. This class is not for production use;
 * it allows you to check whether your beanType selection strategy is correct without
 * having to connect to a real data source.
 */
public class BeanCacheUpdaterFactoryNoFetch implements BeanCacheUpdaterFactory {

	private final BeanTypeSelectionStrategy beanTypeSelectionStrategy;
	
	public BeanCacheUpdaterFactoryNoFetch(BeanTypeSelectionStrategy beanTypeSelectionStrategy) {
	    this.beanTypeSelectionStrategy = beanTypeSelectionStrategy;
    }

	@Override
	public <BeanType> BeanCacheUpdater<BeanType> createBeanCacheUpdater(String entityName, Class<BeanType> beanType) {
		return new BeanCacheUpdaterNoFetch<BeanType>(entityName, beanType);
	}

	@Override
	public BeanCacheUpdater<?> createBeanCacheUpdater(String entityName) throws ClassNotFoundException {
		return createBeanCacheUpdater(entityName, beanTypeSelectionStrategy.chooseBeanType(entityName));
	}

}
