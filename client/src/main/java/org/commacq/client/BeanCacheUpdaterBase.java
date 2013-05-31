package org.commacq.client;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Base implementation for a BeanCacheUpdater. Implements Cache Observer features.
 * 
 * Cache Observers should only be notified of changes once the bean cache is live,
 * which means when the Manager has been created.
 */
public abstract class BeanCacheUpdaterBase<BeanType> implements BeanCacheUpdater<BeanType> {
	
	protected Set<CacheObserver<BeanType>> cacheObservers = new HashSet<>();
	
	@Override
	public void addCacheObserver(CacheObserver<BeanType> cacheObserver) {
		synchronized(cacheObservers) {
			cacheObservers.add(cacheObserver);
		}
	}
	
	@Override
	public void removeCacheObserver(CacheObserver<BeanType> cacheObserver) {
		synchronized(cacheObservers) {
			cacheObservers.add(cacheObserver);
		}
	}
	
	/**
	 * Subclasses use this method to notify observers of changes.
	 */
	protected void notifyCacheObservers(Map<String, BeanType> updated, Set<String> deleted) {
		synchronized(cacheObservers) {
			for(CacheObserver<BeanType> cacheObserver : cacheObservers) {
				if(!updated.isEmpty()) {
					cacheObserver.beansUpdated(updated);
				}
				if(!deleted.isEmpty()) {
					cacheObserver.beansDeleted(deleted);
				}
			}
		}
	}
	
}
