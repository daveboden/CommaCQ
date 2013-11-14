package org.commacq.layer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.concurrent.Immutable;

import org.commacq.CsvDataSource;
import org.commacq.CsvDataSourceLayer;
import org.commacq.CsvLineCallback;

/**
 * Joins layers together so that they can be addressed as a single block.
 * 
 * An example is joining together the layers originating from two separate
 * databases.
 */
@Immutable
public class CsvDataSourceLayerUnion implements CsvDataSourceLayer {
	
	protected final SortedSet<String> entityIds;
	protected final Map<String, ? extends CsvDataSourceLayer> mapping;
	private final Map<String, CsvDataSource> sourceMapping;
	
	public CsvDataSourceLayerUnion(Collection<? extends CsvDataSourceLayer> collection) {
		SortedSet<String> entityIds = new TreeSet<String>();
		Map<String, CsvDataSourceLayer> mapping = new HashMap<String, CsvDataSourceLayer>();
		Map<String, CsvDataSource> sourceMapping = new HashMap<String, CsvDataSource>();
		for(CsvDataSourceLayer layer : collection) {
			entityIds.addAll(layer.getEntityIds());
			for(String entityId : layer.getEntityIds()) {
				mapping.put(entityId, layer);
			}
			sourceMapping.putAll(layer.getMap());
		}
		
		this.entityIds = Collections.unmodifiableSortedSet(entityIds);
		this.mapping = Collections.unmodifiableMap(mapping);
		this.sourceMapping = Collections.unmodifiableMap(sourceMapping);
	}

	@Override
	public SortedSet<String> getEntityIds() {
		return entityIds;
	}

	@Override
	public String getCsvEntry(String entityId, String id) {
		return getLayer(entityId).getCsvEntry(entityId, id);
	}
	
	@Override
	public CsvDataSource getCsvDataSource(String entityId) {
		return getLayer(entityId).getCsvDataSource(entityId);
	}

	@Override
	public Map<String, CsvDataSource> getMap() {
		return sourceMapping;
	}
	
	protected CsvDataSourceLayer getLayer(String entityId) {
		CsvDataSourceLayer layer = mapping.get(entityId);
		if(layer == null) {
			throw new RuntimeException("Unknown entity id: " + entityId);
		}
		return layer;
	}
	
	@Override
	public void subscribe(CsvLineCallback callback) {
		for(CsvDataSourceLayer layer : mapping.values()) {
			layer.subscribe(callback);
		}
	}
	
	@Override
	public void subscribe(CsvLineCallback callback, List<String> entityIds) {
		for(CsvDataSourceLayer layer : mapping.values()) {
			layer.subscribe(callback, entityIds);
		}
	}
	
	@Override
	public void subscribe(CsvLineCallback callback, String entityId) {
		for(CsvDataSourceLayer layer : mapping.values()) {
			layer.subscribe(callback, entityId);
		}
	}
	
	@Override
	public void getAllCsvLinesAndSubscribe(CsvLineCallback callback) {
		for(CsvDataSourceLayer layer : mapping.values()) {
			layer.getAllCsvLinesAndSubscribe(callback);
		}
	}
	
	@Override
	public void getAllCsvLinesAndSubscribe(CsvLineCallback callback, List<String> entityIds) {
		for(CsvDataSourceLayer layer : mapping.values()) {
			layer.getAllCsvLinesAndSubscribe(callback, entityIds);
		}
	}
	
	@Override
	public void getAllCsvLinesAndSubscribe(CsvLineCallback callback, String entityId) {
		for(CsvDataSourceLayer layer : mapping.values()) {
			layer.getAllCsvLinesAndSubscribe(callback, entityId);
		}
	}
	
	@Override
	public void unsubscribe(CsvLineCallback callback) {
		for(CsvDataSourceLayer layer : mapping.values()) {
			layer.unsubscribe(callback);
		}		
	}
}
