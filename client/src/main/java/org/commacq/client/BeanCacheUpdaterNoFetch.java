package org.commacq.client;

import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanCacheUpdaterNoFetch<BeanType> implements BeanCacheUpdater<BeanType> {

	private static Logger logger = LoggerFactory.getLogger(BeanCacheUpdaterNoFetch.class);
	
	private final String entityId;
	private final Class<BeanType> beanType;
	
	public BeanCacheUpdaterNoFetch(String entityId, Class<BeanType> beanType) {
	    this.entityId = entityId;
	    this.beanType = beanType;
    }

	@Override
    public Map<String, BeanType> getInitializedBeanCache() {
		return Collections.emptyMap();
    }

	@Override
    public Class<BeanType> getBeanType() {
	    return beanType;
    }

	@Override
    public String getEntityId() {
		return entityId;
    }

	@Override
    public void addCacheObserver(CacheObserver<BeanType> cacheObserver) {    
		logger.info("addCacheObserver not supported in the NoFetch implementation of BeanCacheUpdater");
    }

	@Override
    public void removeCacheObserver(CacheObserver<BeanType> cacheObserver) {
		logger.info("removeCacheObserver not supported in the NoFetch implementation of BeanCacheUpdater");
    }
	
}
