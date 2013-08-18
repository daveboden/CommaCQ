package org.commacq;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.commacq.CsvMarshaller.CsvLine;

public class CsvDataSourceUnion implements CsvDataSource {
	
	private final SortedSet<String> entityIds = new TreeSet<>();
	private final Map<String, CsvDataSource> entityIdToSource = new HashMap<>();
	
	public CsvDataSourceUnion(List<CsvDataSource> sources) {
		for(CsvDataSource source : sources) {
			for(String entityId : source.getEntityIds()) {
				CsvDataSource alreadyInMap = entityIdToSource.put(entityId, source);
				if(alreadyInMap != null) {
					String error = String.format("Duplicate entity id %s in sources %s and %s",
							                     entityId, alreadyInMap, source);
					throw new RuntimeException(error);
				}
				entityIds.add(entityId);
			}
		}
	}

	@Override
	public SortedSet<String> getEntityIds() {
		return Collections.unmodifiableSortedSet(entityIds);
	}

	@Override
	public Map<String, CsvCache> createInitialCaches() {
		Map<String, CsvCache> caches = new HashMap<>();
		for(CsvDataSource source : entityIdToSource.values()) {
			caches.putAll(source.createInitialCaches());
		}
		return caches;
	}

	@Override
	public CsvCache createInitialCache(String entityId) {
		return getFromMapAndCheck(entityId).createInitialCache(entityId);
	}

	@Override
	public List<CsvLine> getCsvLines(String entityId, Collection<String> ids) {
		return getFromMapAndCheck(entityId).getCsvLines(entityId, ids);
	}
	
	@Override
	public List<CsvLine> getCsvLinesForGroup(String entityId, String group, String idWithinGroup) {
	    return getFromMapAndCheck(entityId).getCsvLinesForGroup(entityId, group, idWithinGroup);
	}

	@Override
	public CsvLine getCsvLine(String entityId, String id) {
		return getFromMapAndCheck(entityId).getCsvLine(entityId, id);
	}

	private CsvDataSource getFromMapAndCheck(String entityId) {
		CsvDataSource source = entityIdToSource.get(entityId);
		if(source == null) {
			throw new RuntimeException("Unknown entityId: " + entityId);
		}
		return source;
	}
	
}
