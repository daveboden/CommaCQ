package org.commacq.layer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.commacq.CsvDataSource;
import org.commacq.CsvLineCallback;
import org.commacq.CsvLineCallbackSingleImpl;
import org.commacq.CsvUpdateBlockException;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * Defines a set of CsvDataSources. Sources within the same layer
 * can be updated together (transactionally). An example of the use
 * of layers is creating a cache CsvDataSource for each "real"
 * data source in a layer.
 */
@ManagedResource
public class DataSourceCollectionLayer extends AbstractUpdatableLayer {
	
	private ThreadLocal<CsvLineCallbackSingleImpl> csvLineCallbackSingleImplLocal = new ThreadLocal<CsvLineCallbackSingleImpl>() {
		protected CsvLineCallbackSingleImpl initialValue() {
			return new CsvLineCallbackSingleImpl();
		};
	};

	private final SortedSet<String> entityIds = new TreeSet<String>();
	//Unmodifiable wrapper to give to clients
	private final SortedSet<String> entityIdsUnmodifiable = Collections.unmodifiableSortedSet(entityIds);
	
	private final Map<String, CsvDataSource> csvDataSourceMap = new HashMap<String, CsvDataSource>();
	//Unmodifiable wrapper to give to clients
	private final Map<String, CsvDataSource> csvDataSourceMapUnmodifiable = Collections.unmodifiableMap(csvDataSourceMap);	
	
	public DataSourceCollectionLayer(Collection<? extends CsvDataSource> csvDataSources) {
		for(CsvDataSource source : csvDataSources) {
			boolean newEntry = entityIds.add(source.getEntityId());
			if(!newEntry) {
				throw new RuntimeException("Duplicate entityId in layer: " + source.getEntityId());
			}
			csvDataSourceMap.put(source.getEntityId(), source);
		}
	}
	
	@Override
	@ManagedAttribute
	public SortedSet<String> getEntityIds() {
		return entityIdsUnmodifiable;
	}
	
	@Override
	public String getColumnNamesCsv(String entityId) {
		return getCsvDataSource(entityId).getColumnNamesCsv();
	}
	
	@ManagedOperation
	public String getCsvEntry(String entityId, String id) {
		CsvLineCallbackSingleImpl callback = csvLineCallbackSingleImplLocal.get();  
	    getCsvDataSource(entityId).getCsvLine(id, callback);
	    return callback.getCsvLineAndClear().getCsvLine();
	}
	
	@Override
	@ManagedOperation
	public String pokeCsvEntry(String entityId, String id) throws CsvUpdateBlockException {
		CsvDataSource source = getCsvDataSource(entityId);
		if(!(source instanceof UpdatableLayer)) {
			throw new RuntimeException("Not an updatable data source: " + entityId);
		}
		
		UpdatableLayer updatableDataSource = (UpdatableLayer)source;
		updatableDataSource.startUpdateBlock(entityId, source.getColumnNamesCsv());
		updatableDataSource.updateUntrusted(entityId, id);
		updatableDataSource.finish();
		
		return getCsvEntry(entityId, id);
	}
	
	@Override
	public CsvDataSource getCsvDataSource(String entityId) {
		return csvDataSourceMap.get(entityId);
	}
	
	@Override
	public Map<String, CsvDataSource> getMap() {
		return csvDataSourceMapUnmodifiable;
	}
	
	@Override
	@ManagedOperation
	public void reload(String entityId) throws CsvUpdateBlockException {
		CsvDataSource csvDataSource = getMap().get(entityId);
		if(csvDataSource == null) {
			throw new IllegalArgumentException("entityId " + entityId + " not recognised");
		}
		if(csvDataSource instanceof UpdatableLayer) {
			((UpdatableLayer)csvDataSource).reload(entityId);
		} else {
			throw new IllegalArgumentException("entityId " + entityId + " not updatable");
		}
	}
	
	@Override
	@ManagedOperation
	public void reloadAll() throws CsvUpdateBlockException {
		for(String entityId : entityIds) {
			reload(entityId);
		}
	}

	@Override
	public void getAllCsvLines(CsvLineCallback callback) {
		for(String entityId : entityIds) {
			getCsvDataSource(entityId).getAllCsvLines(callback);
		}
	}

	@Override
	public void getAllCsvLines(Collection<String> entityIds, CsvLineCallback callback) {
		for(String entityId : entityIds) {
			getCsvDataSource(entityId).getAllCsvLines(callback);
		}
	}

	@Override
	public void getAllCsvLines(String entityId, CsvLineCallback callback) {
		getCsvDataSource(entityId).getAllCsvLines(callback);
	}

	@Override
	public void getCsvLines(String entityId, Collection<String> ids, CsvLineCallback callback) {
		getCsvDataSource(entityId).getCsvLines(ids, callback);
	}

	@Override
	public void getCsvLine(String entityId, String id, CsvLineCallback callback) {
		getCsvDataSource(entityId).getCsvLine(id, callback);
	}

	@Override
	public void getCsvLinesForGroup(String entityId, String group, String idWithinGroup, CsvLineCallback callback) {
		getCsvDataSource(entityId).getCsvLinesForGroup(group, idWithinGroup, callback);
	}
    
}
