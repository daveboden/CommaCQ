package org.commacq.cache.csv;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import lombok.extern.slf4j.Slf4j;

import org.commacq.BlockCallback;
import org.commacq.CsvLine;
import org.commacq.CsvUpdateBlockException;
import org.commacq.LineCallback;
import org.commacq.layer.AbstractSubscribeLayer;
import org.commacq.layer.SubscribeLayer;
import org.springframework.jmx.export.annotation.ManagedOperation;

/**
 * Takes a collection of CsvDataSources and prepares a Cache implementation
 * that wraps them.
 */
@Slf4j
public class CacheLayer extends AbstractSubscribeLayer {
	
    private final Object csvCacheMonitor = new Object(); //Switching the csvCache reference synchronises on this monitor
    private final Map<String, CsvDataSourceCache> caches;
    private final SortedSet<String> entityIds;
    
	public CacheLayer(SubscribeLayer sourceLayer) {
		caches = new HashMap<>(sourceLayer.getEntityIds().size());
		this.entityIds = sourceLayer.getEntityIds();
		for(String entityId : sourceLayer.getEntityIds()) {
			CsvDataSourceCache cache = new CsvDataSourceCache(entityId);
			caches.put(entityId, cache);
		}
		
		CsvCacheFactoryInitialLoad initialLoad = new CsvCacheFactoryInitialLoad();
		try {
			initialLoad.start(entityIds);
			for(String entityId : entityIds) {
				initialLoad.startBulkUpdate(entityId, sourceLayer.getColumnNamesCsv(entityId));
			}
			sourceLayer.getAllCsvLinesAndSubscribe(initialLoad);
			initialLoad.finish();
		} catch (CsvUpdateBlockException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public CacheLayer(SubscribeLayer sourceLayer, Collection<String> entityIds) {
		caches = new HashMap<>(entityIds.size());
		this.entityIds = new TreeSet<String>(entityIds);
		for(String entityId : entityIds) {
			CsvDataSourceCache cache = new CsvDataSourceCache(entityId);
			caches.put(entityId, cache);
		}
		
		CsvCacheFactoryInitialLoad initialLoad = new CsvCacheFactoryInitialLoad();
		sourceLayer.getAllCsvLinesAndSubscribe(entityIds, initialLoad);
	}
	
    @Override
	public SortedSet<String> getEntityIds() {
    	return entityIds;
	}
    
    @Override
    public String getColumnNamesCsv(String entityId) {
    	return caches.get(entityId).getColumnNamesCsv();
    }

	@ManagedOperation
	public String getCsvEntry(String entityId, String id) {
		return caches.get(entityId).csvCache.getLine(id).getCsvLine();
	}

	@Override
	public void getAllCsvLines(LineCallback callback) {
    	for(CsvDataSourceCache cache : caches.values()) {
			cache.getAllCsvLines(callback);
    	}
	}

	@Override
	public void getAllCsvLines(Collection<String> entityIds, LineCallback callback) {
    	for(String entityId : entityIds) {
			CsvDataSourceCache cache = caches.get(entityId);
			cache.getAllCsvLines(callback);
    	}		
	}

	@Override
	public void getAllCsvLines(String entityId, LineCallback callback) {
		CsvDataSourceCache cache = caches.get(entityId);
		cache.getAllCsvLines(callback);
	}

	@Override
	public void getCsvLines(String entityId, Collection<String> ids, LineCallback callback) {
		CsvDataSourceCache cache = caches.get(entityId);
		cache.getCsvLines(ids, callback);
	}

	@Override
	public void getCsvLine(String entityId, String id, LineCallback callback) {
		CsvDataSourceCache cache = caches.get(entityId);
		cache.getCsvLine(id, callback);
	}

	@Override
	public void getCsvLinesForGroup(String entityId, String group, String idWithinGroup, LineCallback callback) {
		CsvDataSourceCache cache = caches.get(entityId);
		cache.getCsvLinesForGroup(group, idWithinGroup, callback);
	}


	/**
     * The initial load does not update any subscribers until the initial load has completed; the
     * cache is not open for business and no subscribers have had the opportunity to add themselves.
     */
    private final class CsvCacheFactoryInitialLoad implements BlockCallback {
    	
    	//Either points to a local cache where we're preparing a refresh
    	//or points to the main csvCache that's in operation.
    	private Map<String, CsvCache> localCsvCache = new HashMap<>();
    	
    	@Override
    	public void processUpdate(String entityId, String columnNamesCsv, CsvLine csvLine) throws CsvUpdateBlockException { 
    		localCsvCache.get(entityId).updateLine(csvLine);
    		composite.processUpdate(entityId, columnNamesCsv, csvLine);
    	}
    	
    	@Override
    	public void processRemove(String entityId, String columnNamesCsv, String id) throws CsvUpdateBlockException {
    		localCsvCache.get(entityId).removeId(id);
    		composite.processRemove(entityId, columnNamesCsv, id);
    	}
    	
    	@Override
    	public void startBulkUpdate(String entityId, String columnNamesCsv) throws CsvUpdateBlockException {
    		log.debug("Initialising local CsvCache with columns {} with context {}.", columnNamesCsv);
    		localCsvCache.put(entityId, new CsvCache(entityId, columnNamesCsv));
    		composite.startBulkUpdate(entityId, columnNamesCsv);
    	}
    	
    	@Override
    	public void finish() throws CsvUpdateBlockException {
    		for(CsvDataSourceCache cache : caches.values()) {
    			String entityId = cache.getEntityId();
	    		if(localCsvCache.get(entityId) != cache.csvCache) {
		    		log.debug("Refresh completed.");
		    		synchronized(csvCacheMonitor) {
		    			cache.csvCache = localCsvCache.get(entityId);
		    			csvCacheMonitor.notify();
		    		}
	    		}
    		}
    		composite.finish();
    	}
    	
    	@Override
    	public void cancel() {
    		for(CsvDataSourceCache cache : caches.values()) {
    			String entityId = cache.getEntityId();
	    		if(localCsvCache.get(entityId) != cache.csvCache) {
		    		log.warn("Cancelling update");
		    		synchronized(csvCacheMonitor) {
		    			localCsvCache.put(entityId, cache.csvCache);
		    			csvCacheMonitor.notify();
		    		}
	    		}
    		}
    		composite.cancel();
    	}
    	
    	@Override
    	public void start(Collection<String> entityIds) throws CsvUpdateBlockException {
    		composite.start(entityIds);
    	}
    	
    	@Override
    	public void startBulkUpdateForGroup(String entityId, String group, String idWithinGroup) throws CsvUpdateBlockException {
    		throw new UnsupportedOperationException("Group updates not yet supported.");
    	}
    }	
}
