package org.commacq.client;

import java.util.Map;

/**
 * In the same package as the manager so that the
 * this updater class, and not all the plugin loader
 * classes, has permission to setup the cache on the
 * manager.
 */
public interface BeanCacheUpdater<BeanType> {

	/**
	 * Chooses a bean cache type (immutable? concurrent updates required?)
	 * Primes the bean cache using the method of its choice.
	 * Subscribes for updates if applicable. If so, it maintains a reference
	 * to the bean cache in order to effect the updates.
	 * 
	 * This method is only called once by the manager during the manager's construction.
	 */
	Map<String, BeanType> getInitializedBeanCache();
	
	Class<BeanType> getBeanType();
	String getEntityId();
	
	void addCacheObserver(CacheObserver<BeanType> cacheObserver);
	void removeCacheObserver(CacheObserver<BeanType> cacheObserver);
	
}