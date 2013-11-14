package org.commacq.cache.csv;

import java.util.List;

import org.commacq.CsvDataSourceLayer;
import org.commacq.layer.LayerFactory;

public class CsvDataSourceCacheFactory implements LayerFactory {
    
	public CsvDataSourceLayer createLayer(CsvDataSourceLayer sourceLayer) {
		return new CacheLayer(sourceLayer);
	}
	
	@Override
	public CsvDataSourceLayer createLayer(CsvDataSourceLayer sourceLayer, List<String> entityIds) {
		return new CacheLayer(sourceLayer, entityIds);
	}
	
}
