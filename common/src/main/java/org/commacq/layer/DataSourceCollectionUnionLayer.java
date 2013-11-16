package org.commacq.layer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.concurrent.Immutable;

import org.commacq.CsvDataSource;
import org.commacq.CsvUpdateBlockException;
import org.commacq.LineCallback;

/**
 * Joins layers together so that they can be addressed as a single block.
 * 
 * An example is joining together the layers originating from two separate
 * databases.
 */
@Immutable
public class DataSourceCollectionUnionLayer extends AbstractUpdatableLayer {
	
	protected final SortedSet<String> entityIds;
	protected final Map<String, DataSourceCollectionLayer> mapping;
	private final Map<String, CsvDataSource> sourceMapping;
	
	public DataSourceCollectionUnionLayer(Collection<DataSourceCollectionLayer> collection) {
		SortedSet<String> entityIds = new TreeSet<String>();
		Map<String, DataSourceCollectionLayer> mapping = new HashMap<>();
		Map<String, CsvDataSource> sourceMapping = new HashMap<String, CsvDataSource>();
		for(DataSourceCollectionLayer layer : collection) {
			entityIds.addAll(layer.getEntityIds());
			for(String entityId : layer.getEntityIds()) {
				mapping.put(entityId, layer);
			}
			sourceMapping.putAll(layer.getMap());
			layer.subscribe(composite); //Act as a pass-through for all updates
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
	public String getColumnNamesCsv(String entityId) {
		return sourceMapping.get(entityId).getColumnNamesCsv();
	}
	
	@Override
	public CsvDataSource getCsvDataSource(String entityId) {
		return sourceMapping.get(entityId);
	}

	@Override
	public Map<String, CsvDataSource> getMap() {
		return sourceMapping;
	}
	
	protected DataSourceCollectionLayer getLayer(String entityId) {
		DataSourceCollectionLayer layer = mapping.get(entityId);
		if(layer == null) {
			throw new RuntimeException("Unknown entity id: " + entityId);
		}
		return layer;
	}

	@Override
	public String pokeCsvEntry(String entityId, String id) throws CsvUpdateBlockException {
		return getLayer(entityId).pokeCsvEntry(entityId, id);
	}

	@Override
	public void reloadAll() throws CsvUpdateBlockException {
		for(DataSourceCollectionLayer layer : mapping.values()) {
			layer.reloadAll();
		}
	}

	@Override
	public void getAllCsvLines(LineCallback callback) {
		for(DataSourceCollectionLayer layer : mapping.values()) {
			layer.getAllCsvLines(callback);
		}
	}

	@Override
	public void getAllCsvLines(Collection<String> entityIds, LineCallback callback) {
		for(DataSourceCollectionLayer layer : mapping.values()) {
			layer.getAllCsvLines(entityIds, callback);
		}
	}

	@Override
	public void getAllCsvLines(String entityId, LineCallback callback) {
		getLayer(entityId).getAllCsvLines(entityId, callback);
	}

	@Override
	public void getCsvLines(String entityId, Collection<String> ids, LineCallback callback) {
		getLayer(entityId).getCsvLines(entityId, ids, callback);		
	}

	@Override
	public void getCsvLine(String entityId, String id, LineCallback callback) {
		getLayer(entityId).getCsvLine(entityId, id, callback);		
	}

	@Override
	public void getCsvLinesForGroup(String entityId, String group, String idWithinGroup, LineCallback callback) {
		getLayer(entityId).getCsvLinesForGroup(entityId, group, idWithinGroup, callback);
	}
}
