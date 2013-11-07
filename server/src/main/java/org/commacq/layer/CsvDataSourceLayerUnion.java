package org.commacq.layer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.concurrent.Immutable;

import org.commacq.CsvDataSource;
import org.commacq.CsvDataSourceLayer;
import org.commacq.CsvUpdatableDataSource;
import org.commacq.CsvUpdateBlockException;
import org.springframework.jmx.export.annotation.ManagedOperation;

/**
 * Joins layers together so that they can be addressed as a single block.
 * 
 * An example is joining together the layers originating from two separate
 * databases.
 */
@Immutable
public class CsvDataSourceLayerUnion implements CsvDataSourceLayer {
	
	private final SortedSet<String> entityIds;
	private final Map<String, CsvDataSourceLayer> mapping;
	private final Map<String, CsvDataSource> sourceMapping;
	
	public CsvDataSourceLayerUnion(Collection<CsvDataSourceLayer> collection) {
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
	public String pokeCsvEntry(String entityId, String id) throws CsvUpdateBlockException {
		return getLayer(entityId).pokeCsvEntry(entityId, id); 
	}

	@Override
	public CsvDataSource getCsvDataSource(String entityId) {
		return getLayer(entityId).getCsvDataSource(entityId);
	}

	@Override
	public Map<String, CsvDataSource> getMap() {
		return sourceMapping;
	}
	
	private CsvDataSourceLayer getLayer(String entityId) {
		CsvDataSourceLayer layer = mapping.get(entityId);
		if(layer == null) {
			throw new RuntimeException("Unknown entity id: " + entityId);
		}
		return layer;
	}
	
	@Override
	@ManagedOperation
	public void reload(String entityId) throws CsvUpdateBlockException {
		CsvDataSource csvDataSource = getMap().get(entityId);
		if(csvDataSource instanceof CsvUpdatableDataSource) {
			((CsvUpdatableDataSource)csvDataSource).reload();
		}
	}
	
	@Override
	@ManagedOperation
	public void reloadAll() throws CsvUpdateBlockException {
		for(String entityId : entityIds) {
			reload(entityId);
		}
	}
}
