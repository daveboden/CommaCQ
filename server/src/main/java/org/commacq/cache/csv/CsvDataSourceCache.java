package org.commacq.cache.csv;

import java.util.Collection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.commacq.CsvDataSource;
import org.commacq.CsvLine;
import org.commacq.CsvUpdateBlockException;
import org.commacq.LineCallback;

/**
 * Maintains a cache which looks like a CsvDataSource and can be notified of updates.
 * 
 * A bulk update results in a new cache being created. When the bulk update has finished,
 * the cache is swapped in as the current cache. All further updates go into the new
 * cache.
 */
@RequiredArgsConstructor
public class CsvDataSourceCache implements CsvDataSource {
    
    protected volatile CsvCache csvCache; //Not usable until first initial load has completed
    
    @Getter
    private final String entityId;
    
    /**
     * Fetches from the cache.
     */
    @Override
    public void getAllCsvLines(LineCallback callback) {
    	try {
			csvCache.visitAll(callback);
		} catch (CsvUpdateBlockException ex) {
			throw new RuntimeException(ex);
		}
    }
    
    @Override
    public void getCsvLine(String id, LineCallback callback) {
    	CsvLine csvLine = csvCache.getLine(id);
   		try {
   			if(csvLine != null) {
				callback.processUpdate(csvCache.getEntityId(), csvCache.getColumnNamesCsv(), csvLine);
    		} else {
    			callback.processRemove(csvCache.getEntityId(), csvCache.getColumnNamesCsv(), id);
    		}
		} catch (CsvUpdateBlockException ex) {
			throw new RuntimeException(ex);
		}
    }
    
    @Override
    public void getCsvLines(Collection<String> ids, LineCallback callback) {
    	csvCache.visitIds(callback, ids);
    }
    
    @Override
    public void getCsvLinesForGroup(String group, String idWithinGroup, LineCallback callback) {
    	csvCache.visitGroup(callback, group, idWithinGroup);
    }
    
    @Override
    public String getColumnNamesCsv() {
    	return csvCache.getColumnNamesCsv();
    }
    
}