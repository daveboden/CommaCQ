package org.commacq.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.commacq.CsvDataSource;
import org.commacq.CsvLine;
import org.commacq.CsvLineCallback;
import org.commacq.CsvUpdateBlockException;

/**
 * Converts a CsvDataSource into a source of beans.
 * 
 * Fetches all info from the data source when init() is called.
 */
public class BeanCache<BeanType> {

	private final CsvDataSource csvDataSource;
	private final CsvToBeanConverter<BeanType> csvToBeanConverter;
	
	final Map<String, BeanType> cache = new HashMap<String, BeanType>();
	
	public BeanCache(final CsvDataSource csvDataSource, final CsvToBeanConverter<BeanType> csvToBeanConverter) {
		this.csvDataSource = csvDataSource;
		this.csvToBeanConverter = csvToBeanConverter;
		//Trigger the callback for every line from the CsvDataSource
		csvDataSource.getAllCsvLinesAndSubscribe(beanCsvLineCallback);
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
		return csvDataSource.getEntityId();
	}
	
	private BeanCsvLineCallback beanCsvLineCallback = new BeanCsvLineCallback();
	
	private class BeanCsvLineCallback implements CsvLineCallback {
		private boolean bulkUpdate;
		private Map<String, BeanType> updated = new HashMap<String, BeanType>();
		private Set<String> deleted = new HashSet<String>();
		
		@Override
		public void processUpdate(String columnNamesCsv, CsvLine csvLine) throws CsvUpdateBlockException {
			BeanType bean = csvToBeanConverter.getBean(columnNamesCsv, csvLine);
			updated.put(csvLine.getId(), bean);
		}
		
		@Override
		public void processRemove(String id) throws CsvUpdateBlockException {
			deleted.add(id);
		}
		
		@Override
		public void startBulkUpdate(String columnNamesCsv) throws CsvUpdateBlockException {
			cache.clear();
		}
		
		@Override
		public void startUpdateBlock(String columnNamesCsv) throws CsvUpdateBlockException {
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
		public void finishUpdateBlock() throws CsvUpdateBlockException {
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
		public void startBulkUpdateForGroup(String group, String idWithinGroup) throws CsvUpdateBlockException {
			//TODO clear down all items from the cache that are marked as in the group
			//TODO clear group index
		}
	}
	
}
