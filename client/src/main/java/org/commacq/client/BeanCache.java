package org.commacq.client;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.commacq.CsvLine;
import org.commacq.BlockCallback;
import org.commacq.CsvUpdateBlockException;
import org.commacq.layer.SubscribeLayer;

/**
 * Converts a CsvDataSource into a source of beans.
 * 
 * Fetches all info from the data source when init() is called.
 */
public class BeanCache<BeanType> {

	private final SubscribeLayer layer;
	private final String entityId;
	private final CsvToBeanConverter<BeanType> csvToBeanConverter;
	
	final Map<String, BeanType> cache = new HashMap<String, BeanType>();
	
	public BeanCache(final SubscribeLayer layer, final String entityId, final CsvToBeanConverter<BeanType> csvToBeanConverter) {
		this.layer = layer;
		this.entityId = entityId;
		this.csvToBeanConverter = csvToBeanConverter;
		//Trigger the callback for every line from the CsvDataSource
		
		try {
			beanCsvLineCallback.start(Collections.singleton(entityId));
			layer.getAllCsvLinesAndSubscribe(entityId, beanCsvLineCallback);
			beanCsvLineCallback.finish();
		} catch (CsvUpdateBlockException ex) {
			throw new RuntimeException(ex);
		}
		
	}
	
	protected Set<CacheObserver<BeanType>> cacheObservers = new HashSet<>();
	
	public void addCacheObserver(CacheObserver<BeanType> cacheObserver) {
		synchronized(cacheObservers) {
			cacheObservers.add(cacheObserver);
		}
	}
	
	public void removeCacheObserver(CacheObserver<BeanType> cacheObserver) {
		synchronized(cacheObservers) {
			cacheObservers.add(cacheObserver);
		}
	}
	
	private void notifyCacheObservers(Map<String, BeanType> updated, Set<String> deleted) {
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
	
	public BeanType get(String id) {
		return cache.get(id);
	}
	
	public Map<String, BeanType> getAllMappings() {
		return Collections.unmodifiableMap(cache);
	}
	
	public Class<BeanType> getBeanType() {
		return csvToBeanConverter.getBeanType();
	}
	
	public String getEntityId() {
		return entityId;
	}
	
	private BeanCsvLineCallback beanCsvLineCallback = new BeanCsvLineCallback();
	
	private class BeanCsvLineCallback implements BlockCallback {
		private boolean bulkUpdate;
		private Map<String, BeanType> updated = new HashMap<String, BeanType>();
		private Set<String> deleted = new HashSet<String>();
		
		@Override
		public void processUpdate(String entityId, String columnNamesCsv, CsvLine csvLine) throws CsvUpdateBlockException {
			BeanType bean = csvToBeanConverter.getBean(columnNamesCsv, csvLine);
			updated.put(csvLine.getId(), bean);
		}
		
		@Override
		public void processRemove(String entityId, String columnNamesCsv, String id) throws CsvUpdateBlockException {
			deleted.add(id);
		}
		
		@Override
		public void startBulkUpdate(String entityId, String columnNamesCsv) throws CsvUpdateBlockException {
			cache.clear();
		}
		
		@Override
		public void start(Collection<String> entityId) throws CsvUpdateBlockException {
			if(!updated.isEmpty()) {
				throw new RuntimeException("Map of updated beans should have been cleared down after the last update block");
			}
			if(!deleted.isEmpty()) {
				throw new RuntimeException("Set of deleted beans should have been cleared down after the last update block");
			}
			if(bulkUpdate) {
				throw new RuntimeException("bulkUpdate flag should have been cleared down after the last update block");				
			}
		}
		
		@Override
		public void finish() throws CsvUpdateBlockException {
			//TODO make this update transactional
			cache.putAll(updated);
			for(String id : deleted) {
				cache.remove(id);
			}
			
			notifyCacheObservers(updated, deleted);
			updated.clear();
			deleted.clear();
			bulkUpdate = false;
		}
		
		@Override
		public void cancel() {
			throw new RuntimeException("Update is not transactional so can't cancel");
		}
		
		@Override
		public void startBulkUpdateForGroup(String entityId, String group, String idWithinGroup) throws CsvUpdateBlockException {
			//TODO clear down all items from the cache that are marked as in the group
			//TODO clear group index
		}
	}
	
}
