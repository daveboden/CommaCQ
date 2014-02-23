package org.commacq.layer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.commacq.CsvDataSource;
import org.commacq.CsvDataSourceFactory;
import org.commacq.CsvLineCallbackSingleImpl;
import org.commacq.CsvUpdateBlockException;
import org.commacq.LineCallback;
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
	
	private void init(Collection<? extends CsvDataSource> csvDataSources) {
		for(CsvDataSource source : csvDataSources) {
			boolean newEntry = entityIds.add(source.getEntityId());
			if(!newEntry) {
				throw new RuntimeException("Duplicate entityId in layer: " + source.getEntityId());
			}
			csvDataSourceMap.put(source.getEntityId(), source);
		}		
	}
	
	public DataSourceCollectionLayer(Collection<? extends CsvDataSource> csvDataSources) {
		init(csvDataSources);
	}
	
	public DataSourceCollectionLayer(CsvDataSource csvDataSource) {
		init(Collections.singleton(csvDataSource));
	}
	
	public DataSourceCollectionLayer(CsvDataSourceFactory factory, Collection<String> entityIds) {
		List<CsvDataSource> sources = new ArrayList<CsvDataSource>();
		for(String entityId : entityIds) {
			try {
				sources.add(factory.createCsvDataSource(entityId));
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
		
		init(sources);
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
		start(Collections.singleton(entityId));
		updateUntrusted(entityId, id);
		finish();
		
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
		start(Collections.singleton(entityId));
		startBulkUpdate(entityId, csvDataSource.getColumnNamesCsv());
		csvDataSource.getAllCsvLines(composite);
		finish();
	}
	
	@Override
	@ManagedOperation
	public void reloadAll() throws CsvUpdateBlockException {
		for(String entityId : entityIds) {
			reload(entityId);
		}
	}

	@Override
	public void getAllCsvLines(LineCallback callback) {
		for(String entityId : entityIds) {
			getCsvDataSource(entityId).getAllCsvLines(callback);
		}
	}

	@Override
	public void getAllCsvLines(Collection<String> entityIds, LineCallback callback) {
		for(String entityId : entityIds) {
			getCsvDataSource(entityId).getAllCsvLines(callback);
		}
	}

	@Override
	public void getAllCsvLines(String entityId, LineCallback callback) {
		getCsvDataSource(entityId).getAllCsvLines(callback);
	}

	@Override
	public void getCsvLines(String entityId, Collection<String> ids, LineCallback callback) {
		getCsvDataSource(entityId).getCsvLines(ids, callback);
	}

	@Override
	public void getCsvLine(String entityId, String id, LineCallback callback) {
		getCsvDataSource(entityId).getCsvLine(id, callback);
	}

	@Override
	public void getCsvLinesForGroup(String entityId, String group, String idWithinGroup, LineCallback callback) {
		getCsvDataSource(entityId).getCsvLinesForGroup(group, idWithinGroup, callback);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("DataSourceCollectionLayer[");
		for(String entityId : entityIds) {
			sb.append(entityId);
			sb.append("[").append(getCsvDataSource(entityId).getClass().getSimpleName()).append("] ");
		}
		return sb.toString();
	}
    
}
