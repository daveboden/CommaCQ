package org.commacq.cache.csv;

import java.util.Collection;

import lombok.extern.slf4j.Slf4j;

import org.commacq.CsvDataSource;
import org.commacq.CsvLine;
import org.commacq.CsvLineCallback;
import org.commacq.CsvLineCallbackComposite;
import org.commacq.CsvUpdateBlockException;

/**
 * Maintains a cache which looks like a CsvDataSource and can be notified of updates.
 * 
 * A bulk update results in a new cache being created. When the bulk update has finished,
 * the cache is swapped in as the current cache. All further updates go into the new
 * cache.
 */
@Slf4j
public class CsvDataSourceCache implements CsvDataSource {
    
    private final CsvDataSource csvDataSource;
    private final CsvLineCallbackComposite subscriptionCallback = new CsvLineCallbackComposite();
    
    private volatile CsvCache csvCache; //Not usable until first initial load has completed
    private final Object csvCacheMonitor = new Object(); //Switching the csvCache reference synchronises on this monitor
    
	/**
	 * @param dataSource Underlying datasource that this object is caching.
	 */
    public CsvDataSourceCache(final CsvDataSource csvDataSource) {
    	this.csvDataSource = csvDataSource;
    	
    	CsvCacheFactoryInitialLoad initialLoad = new CsvCacheFactoryInitialLoad();
    	try {
			initialLoad.startUpdateBlock(csvDataSource.getColumnNamesCsv());
			csvDataSource.getAllCsvLinesAndSubscribe(initialLoad);
			initialLoad.finishUpdateBlock();
		} catch (CsvUpdateBlockException ex) {
			throw new RuntimeException(ex);
		}
    	
    	log.debug("Waiting for initial bulk load to complete");
    	try {
    		initialLoad.waitForFirstLoadCompleted();
    		log.debug("New CsvCache has been switched in.");
    	} catch(InterruptedException ex) {
    		log.warn("Interrupted while waiting for bulk update to finish.");
    		Thread.currentThread().interrupt();
    		return;
    	}
    	
    	log.info("Successfully created cache CSV source with entity id: {}", csvDataSource.getEntityId());
    }
    
    @Override
    public void getAllCsvLinesAndSubscribe(CsvLineCallback callback) {
    	subscriptionCallback.addCallback(callback);
    	getAllCsvLines(callback);
    }
    
    @Override
    public void subscribe(CsvLineCallback callback) {
    	subscriptionCallback.addCallback(callback);
    }
    
    @Override
    public void unsubscribe(CsvLineCallback callback) {
    	subscriptionCallback.removeCallback(callback);
    }
    
    @Override
    public String getEntityId() {
    	return csvDataSource.getEntityId();
    }
    
    /**
     * Fetches from the cache.
     */
    @Override
    public void getAllCsvLines(CsvLineCallback callback) {
    	try {
			callback.startUpdateBlock(csvCache.getColumnNamesCsv());
			csvCache.visitAll(callback);
			callback.finishUpdateBlock();
		} catch (CsvUpdateBlockException ex) {
			throw new RuntimeException(ex);
		}
    }
    
    @Override
    public void getCsvLine(String id, CsvLineCallback callback) {
    	CsvLine csvLine = csvCache.getLine(id);
   		try {
   			if(csvLine != null) {
				callback.processUpdate(csvCache.getColumnNamesCsv(), csvLine);
    		} else {
    			callback.processRemove(id);
    		}
		} catch (CsvUpdateBlockException ex) {
			throw new RuntimeException(ex);
		}
    }
    
    @Override
    public void getCsvLines(Collection<String> ids, CsvLineCallback callback) {
    	csvCache.visitIds(callback, ids);
    }
    
    @Override
    public void getCsvLinesForGroup(String group, String idWithinGroup, CsvLineCallback callback) {
    	csvCache.visitGroup(callback, group, idWithinGroup);
    }
    
    @Override
    public String getColumnNamesCsv() {
    	return csvCache.getColumnNamesCsv();
    }
    
    /**
     * The initial load does not update any subscribers until the initial load has completed; the
     * cache is not open for business and no subscribers have had the opportunity to add themselves.
     */
    private final class CsvCacheFactoryInitialLoad implements CsvLineCallback {
    	
    	private boolean firstLoadCompleted = false;
    	//Either points to a local cache where we're preparing a refresh
    	//or points to the main csvCache that's in operation.
    	private CsvCache localCsvCache;
    	
    	@Override
    	public void processUpdate(String columnNamesCsv, CsvLine csvLine) throws CsvUpdateBlockException { 
    		localCsvCache.updateLine(csvLine);
    		subscriptionCallback.processUpdate(columnNamesCsv, csvLine);
    	}
    	
    	@Override
    	public void processRemove(String id) throws CsvUpdateBlockException {
    		localCsvCache.removeId(id);
    		subscriptionCallback.processRemove(id);
    	}
    	
    	@Override
    	public void startBulkUpdate(String columnNamesCsv) throws CsvUpdateBlockException {
    		log.debug("Initialising local CsvCache with columns {} with context {}.", columnNamesCsv);
    		localCsvCache = new CsvCache(columnNamesCsv);
    		subscriptionCallback.startBulkUpdate(columnNamesCsv);
    	}
    	
    	@Override
    	public void startUpdateBlock(String columnNamesCsv) throws CsvUpdateBlockException {
    		//Check for first time it's ever been called
    		if(localCsvCache == null) {
    			localCsvCache = new CsvCache(columnNamesCsv);
    		}
    		subscriptionCallback.startUpdateBlock(columnNamesCsv);
    	}
    	
    	@Override
    	public void finishUpdateBlock() throws CsvUpdateBlockException {
    		if(localCsvCache != csvCache) {
	    		log.debug("Refresh completed.");
	    		synchronized(csvCacheMonitor) {
	    			csvCache = localCsvCache;
	    			firstLoadCompleted = true;
	    			csvCacheMonitor.notify();
	    		}
    		}
    		subscriptionCallback.finishUpdateBlock();
    	}
    	
    	@Override
    	public void cancel() {
    		if(localCsvCache != csvCache) {
	    		log.warn("Cancelling update");
	    		synchronized(csvCacheMonitor) {
	    			localCsvCache = csvCache;
	    			csvCacheMonitor.notify();
	    		}
    		}
    		subscriptionCallback.cancel();
    	}
    	
    	public synchronized void waitForFirstLoadCompleted() throws InterruptedException {
    		synchronized(csvCacheMonitor){
	    		while(!firstLoadCompleted) {
	    			csvCacheMonitor.wait();
	    		}
    		}
    	}
    	
    	@Override
    	public void startBulkUpdateForGroup(String group, String idWithinGroup) throws CsvUpdateBlockException {
    		throw new UnsupportedOperationException("Group updates not yet supported.");
    	}
    }
    
}
