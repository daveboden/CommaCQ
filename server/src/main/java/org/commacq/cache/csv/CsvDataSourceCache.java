package org.commacq.cache.csv;

import java.util.Collection;

import org.commacq.CsvDataSource;
import org.commacq.CsvLine;
import org.commacq.CsvLineCallback;
import org.commacq.CsvUpdateBlockException;

/**
 * Maintains a cache which looks like a CsvDataSource and can be notified of updates.
 * 
 * A bulk update results in a new cache being created. When the bulk update has finished,
 * the cache is swapped in as the current cache. All further updates go into the new
 * cache.
 */
public class CsvDataSourceCache implements CsvDataSource {
    
    protected volatile CsvCache csvCache; //Not usable until first initial load has completed
    
    @Override
    public String getEntityId() {
    	return csvCache.getEntityId();
    }
    
    /**
     * Fetches from the cache.
     */
    @Override
    public void getAllCsvLines(CsvLineCallback callback) {
    	try {
			callback.startUpdateBlock(csvCache.getEntityId(), csvCache.getColumnNamesCsv());
			csvCache.visitAll(callback);
			callback.finish();
		} catch (CsvUpdateBlockException ex) {
			throw new RuntimeException(ex);
		}
    }
    
    @Override
    public void getCsvLine(String id, CsvLineCallback callback) {
    	CsvLine csvLine = csvCache.getLine(id);
   		try {
   			if(csvLine != null) {
				callback.processUpdate(csvCache.getEntityId(), csvCache.getColumnNamesCsv(), csvLine);
    		} else {
    			callback.processRemove(csvCache.getEntityId(), id);
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
    
}
