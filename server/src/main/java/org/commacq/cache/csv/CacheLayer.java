package org.commacq.cache.csv;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import lombok.extern.slf4j.Slf4j;

import org.commacq.CsvLine;
import org.commacq.CsvLineCallback;
import org.commacq.CsvLineCallbackComposite;
import org.commacq.CsvUpdateBlockException;
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
    private final CsvLineCallbackComposite composite = new CsvLineCallbackComposite();
    private final Map<String, CsvDataSourceCache> caches;
    private final SortedSet<String> entityIds;
    
	public CacheLayer(SubscribeLayer sourceLayer) {
		caches = new HashMap<>(sourceLayer.getEntityIds().size());
		this.entityIds = sourceLayer.getEntityIds();
		for(String entityId : sourceLayer.getEntityIds()) {
			CsvDataSourceCache cache = new CsvDataSourceCache();
			caches.put(entityId, cache);
		}
		
		CsvCacheFactoryInitialLoad initialLoad = new CsvCacheFactoryInitialLoad();
		sourceLayer.getAllCsvLinesAndSubscribe(initialLoad);
	}
	
	public CacheLayer(SubscribeLayer sourceLayer, Collection<String> entityIds) {
		caches = new HashMap<>(entityIds.size());
		this.entityIds = new TreeSet<String>(entityIds);
		for(String entityId : entityIds) {
			CsvDataSourceCache cache = new CsvDataSourceCache();
			caches.put(entityId, cache);
		}
		
		CsvCacheFactoryInitialLoad initialLoad = new CsvCacheFactoryInitialLoad();
		sourceLayer.getAllCsvLinesAndSubscribe(entityIds, initialLoad);
	}
	
    @Override
	public SortedSet<String> getEntityIds() {
    	return entityIds;
	}

	@ManagedOperation
	public String getCsvEntry(String entityId, String id) {
		return caches.get(entityId).csvCache.getLine(id).getCsvLine();
	}

	@Override
	public void getAllCsvLines(CsvLineCallback callback) {
    	for(CsvDataSourceCache cache : caches.values()) {
    		try {
				callback.startUpdateBlock(cache.getEntityId(), cache.getColumnNamesCsv());
				cache.getAllCsvLines(callback);
			} catch (CsvUpdateBlockException ex) {
				throw new RuntimeException(ex);
			}
    	}
	}

	@Override
	public void getAllCsvLines(Collection<String> entityIds, CsvLineCallback callback) {
    	for(String entityId : entityIds) {
    		try {
    			CsvDataSourceCache cache = caches.get(entityId);
				callback.startUpdateBlock(entityId, cache.getColumnNamesCsv());
				cache.getAllCsvLines(callback);
			} catch (CsvUpdateBlockException ex) {
				throw new RuntimeException(ex);
			}
    	}		
	}

	@Override
	public void getAllCsvLines(String entityId, CsvLineCallback callback) {
		try {
			CsvDataSourceCache cache = caches.get(entityId);
			callback.startUpdateBlock(entityId, cache.getColumnNamesCsv());
			cache.getAllCsvLines(callback);
		} catch (CsvUpdateBlockException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void getCsvLines(String entityId, Collection<String> ids, CsvLineCallback callback) {
		try {
			CsvDataSourceCache cache = caches.get(entityId);
			callback.startUpdateBlock(entityId, cache.getColumnNamesCsv());
			cache.getCsvLines(ids, callback);
		} catch (CsvUpdateBlockException ex) {
			throw new RuntimeException(ex);
		}		
	}

	@Override
	public void getCsvLine(String entityId, String id, CsvLineCallback callback) {
		try {
			CsvDataSourceCache cache = caches.get(entityId);
			callback.startUpdateBlock(entityId, cache.getColumnNamesCsv());
			cache.getCsvLine(id, callback);
		} catch (CsvUpdateBlockException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void getCsvLinesForGroup(String entityId, String group, String idWithinGroup, CsvLineCallback callback) {
		try {
			CsvDataSourceCache cache = caches.get(entityId);
			callback.startUpdateBlock(entityId, cache.getColumnNamesCsv());
			cache.getCsvLinesForGroup(group, idWithinGroup, callback);
		} catch (CsvUpdateBlockException ex) {
			throw new RuntimeException(ex);
		}
		
	}


	/**
     * The initial load does not update any subscribers until the initial load has completed; the
     * cache is not open for business and no subscribers have had the opportunity to add themselves.
     */
    private final class CsvCacheFactoryInitialLoad implements CsvLineCallback {
    	
    	//Either points to a local cache where we're preparing a refresh
    	//or points to the main csvCache that's in operation.
    	private Map<String, CsvCache> localCsvCache;
    	
    	@Override
    	public void processUpdate(String entityId, String columnNamesCsv, CsvLine csvLine) throws CsvUpdateBlockException { 
    		localCsvCache.get(entityId).updateLine(csvLine);
    		composite.processUpdate(entityId, columnNamesCsv, csvLine);
    	}
    	
    	@Override
    	public void processRemove(String entityId, String id) throws CsvUpdateBlockException {
    		localCsvCache.get(entityId).removeId(id);
    		composite.processRemove(entityId, id);
    	}
    	
    	@Override
    	public void startBulkUpdate(String entityId, String columnNamesCsv) throws CsvUpdateBlockException {
    		log.debug("Initialising local CsvCache with columns {} with context {}.", columnNamesCsv);
    		localCsvCache.put(entityId, new CsvCache(entityId, columnNamesCsv));
    		composite.startBulkUpdate(entityId, columnNamesCsv);
    	}
    	
    	@Override
    	public void startUpdateBlock(String entityId, String columnNamesCsv) throws CsvUpdateBlockException {
    		//Check for first time it's ever been called
    		if(localCsvCache == null) {
    			localCsvCache.put(entityId, new CsvCache(entityId, columnNamesCsv));
    		}
    		composite.startUpdateBlock(entityId, columnNamesCsv);
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
    	public void start() throws CsvUpdateBlockException {		
    	}
    	
    	@Override
    	public void startBulkUpdateForGroup(String entityId, String group, String idWithinGroup) throws CsvUpdateBlockException {
    		throw new UnsupportedOperationException("Group updates not yet supported.");
    	}
    }	
}
