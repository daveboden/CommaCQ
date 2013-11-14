package org.commacq.layer;

import java.util.List;

import org.commacq.CsvDataSourceLayer;

public interface LayerFactory {

	CsvDataSourceLayer createLayer(CsvDataSourceLayer sourceLayer);
	
	/**
	 * Create a layer that only exposes a subset of the entities provided by the source layer.
	 */
	CsvDataSourceLayer createLayer(CsvDataSourceLayer sourceLayer, List<String> entityIds);
	
}
