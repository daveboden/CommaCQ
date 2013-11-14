package org.commacq.cache.csv;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;

import lombok.extern.slf4j.Slf4j;

import org.commacq.CsvDataSource;
import org.commacq.CsvDataSourceLayer;
import org.commacq.CsvLine;
import org.commacq.CsvLineCallback;
import org.commacq.CsvLineCallbackComposite;
import org.commacq.CsvUpdateBlockException;

/**
 * Takes a collection of CsvDataSources and prepares a Cache implementation
 * that wraps them.
 */
@Slf4j
public class CacheLayer implements CsvDataSourceLayer {
	
    private final Object csvCacheMonitor = new Object(); //Switching the csvCache reference synchronises on this monitor
    private final CsvLineCallbackComposite composite = new CsvLineCallbackComposite();
    private final Map<String, CsvDataSourceCache> caches;
    
	public CacheLayer(CsvDataSourceLayer sourceLayer) {
		caches = new HashMap<>(sourceLayer.getMap().size());
		for(Entry<String, ? extends CsvDataSource> sourceEntry : sourceLayer.getMap().entrySet()) {
			CsvDataSourceCache cache = new CsvDataSourceCache();
			caches.put(sourceEntry.getKey(), cache);
		}
		
		CsvCacheFactoryInitialLoad initialLoad = new CsvCacheFactoryInitialLoad();
		sourceLayer.getAllCsvLinesAndSubscribe(initialLoad);
	}
	
	public CacheLayer(CsvDataSourceLayer sourceLayer, List<String> entityIds) {
		caches = new HashMap<>(entityIds.size());
		for(String entityId : entityIds) {
			CsvDataSourceCache cache = new CsvDataSourceCache();
			caches.put(entityId, cache);
		}
		
		CsvCacheFactoryInitialLoad initialLoad = new CsvCacheFactoryInitialLoad();
		sourceLayer.getAllCsvLinesAndSubscribe(initialLoad);
	}
	
    @Override
	public SortedSet<String> getEntityIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCsvEntry(String entityId, String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CsvDataSource getCsvDataSource(String entityId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, ? extends CsvDataSource> getMap() {
		return caches;
	}
	

    @Override
    public void getAllCsvLinesAndSubscribe(CsvLineCallback callback) {
    	composite.addCallback(callback);
    	try {
    		callback.start();
	    	for(CsvDataSourceCache cache : caches.values()) {
	    		callback.startUpdateBlock(cache.getEntityId(), cache.getColumnNamesCsv());
	    		cache.csvCache.visitAll(callback);
	    	}
	    	callback.finish();
    	} catch(CsvUpdateBlockException ex) {
    		log.warn("Error while communicating with callback. Removing subscriber: {}", callback);
    		composite.removeCallback(callback);
    		callback.cancel();
    	}
    }

	@Override
	public void getAllCsvLinesAndSubscribe(CsvLineCallback callback, List<String> entityIds) {
    	composite.addCallback(callback, entityIds);
    	try {
    		callback.start();
	    	for(String entityId : entityIds) {
	    		callback.startUpdateBlock(entityId, caches.get(entityId).getColumnNamesCsv());
	    		caches.get(entityId).csvCache.visitAll(callback);
	    	}
	    	callback.finish();
    	} catch(CsvUpdateBlockException ex) {
    		log.warn("Error while communicating with callback. Removing subscriber: {}", callback);
    		composite.removeCallback(callback);
    		callback.cancel();
    	}
	}


	@Override
	public void subscribe(CsvLineCallback callback, List<String> entityIds) {
		composite.addCallback(callback, entityIds);
	}
	
	@Override
	public void subscribe(CsvLineCallback callback, String entityId) {
		composite.addCallback(callback, entityId);
	}




	@Override
	public void subscribe(CsvLineCallback callback) {
		composite.addCallback(callback);
	}




	@Override
	public void unsubscribe(CsvLineCallback callback) {
		composite.removeCallback(callback);
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
