package org.commacq.cache.csv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.commacq.CsvDataSource;
import org.commacq.CsvDataSourceLayer;

/**
 * Takes a collection of CsvDataSources and prepares a Cache implementation
 * that wraps them.
 */
public class CsvDataSourceCacheFactory {
    
	public CsvDataSourceLayer create(CsvDataSourceLayer layer) {
		List<CsvDataSource> caches = new ArrayList<CsvDataSource>(layer.getMap().size());
		for(Entry<String, CsvDataSource> sourceEntry : layer.getMap().entrySet()) {
			CsvDataSourceCache cache = new CsvDataSourceCache(sourceEntry.getValue());
			caches.add(cache);
		}
		return new CsvDataSourceLayer(caches);
	}
    
}