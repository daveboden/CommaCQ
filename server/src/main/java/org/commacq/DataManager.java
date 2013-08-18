package org.commacq;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.commacq.CsvMarshaller.CsvLine;

@ManagedResource
public class DataManager {
	
	private static Logger logger = LoggerFactory.getLogger(DataManager.class);

	private final CsvDataSource csvDataSource;
	private final UpdateOutboundHub updateOutboundHub;
	private final Map<String, CsvCache> caches;
	private final SortedSet<String> entityIds;
	
	public DataManager(CsvDataSource csvDataSource, UpdateOutboundHub updateOutboundHub) {
	    this.csvDataSource = csvDataSource;
	    this.updateOutboundHub = updateOutboundHub;
		caches = csvDataSource.createInitialCaches();
		entityIds = csvDataSource.getEntityIds();
		logger.info("Successfully started data manager");
	}
	
	public CsvCache getCsvCache(String entityId) {
		return caches.get(entityId);
	}
	
	@ManagedAttribute
	public SortedSet<String> getEntityIds() {
		return entityIds;
	}
	
	@ManagedOperation
	public String getCsvCacheEntry(String entityId, String id) {
	    return caches.get(entityId).getLine(id);
	}
	
	@ManagedOperation(description=
	        "Fetch a specific single identifier from the data source and " +
	        "push the update to clients regardless of whether the data has changed."
	)
	public void pokeCsvCacheEntry(String entityId, String id) {
	    CsvCache csvCache = caches.get(entityId);
	    CsvLine csvLine = csvDataSource.getCsvLine(entityId, id);
	    Set<String> updatedIds;
	    Set<String> deletedIds;
	    if(csvLine != null) {
	        csvCache.updateLine(csvLine);
	        updatedIds = Collections.singleton(id);
	        deletedIds = Collections.emptySet();
	    } else {
	        csvCache.removeId(id);
	        updatedIds = Collections.emptySet();
	        deletedIds = Collections.singleton(id);
	    }
	    
	    //TODO consider flagging the update message as a manual override. That way,
	    //if a deletion comes through that doesn't already exist in the client cache,
	    //the client can be smart enough to ignore it.
	    
	    updateOutboundHub.sendUpdate(entityId, csvCache, new UpdateCsvCacheResult(updatedIds, deletedIds));
	}
	
	/**
	 * An override method to ensure that there's a way to demand that the server
	 * re-fetches everything from the database and sends out an entire update out to all clients.
	 * This overrides the default behaviour of sending out updates for what's actually changed.
	 * @return
	 */
	public void reloadCacheAndSendBulkUpdate(final String entityId) {
	    CsvCache csvCache = csvDataSource.createInitialCache(entityId);
	    caches.put(entityId, csvCache);
	    updateOutboundHub.sendBulkUpdate(entityId, csvCache);
	}
	
	public UpdateCsvCacheResult updateCsvCache(final String entityId, final List<String> ids) {
	    
	    final List<CsvLine> lines = csvDataSource.getCsvLines(entityId, ids);
	    final Set<String> updatedIds = new HashSet<>();
	    final Set<String> ignoredIds = new HashSet<>();
	    CsvCache cache = caches.get(entityId);
	    
	    for(CsvLine line : lines) {
	        String oldLineValue = cache.updateLine(line);
	        if(oldLineValue == null || !oldLineValue.equals(line.getCsvLine())) {
	            //Detect whether a line has actually changed and only send out an update if it has
	            updatedIds.add(line.getId());
	        } else {
	            logger.debug("Entity did not materially change, so not reporting an update: {}", line.getId());
	            ignoredIds.add(line.getId());
	        }
	    }
		
	    //All the ids minus the ones we've successfully updated or ignored.
	    //What remains needs to be deleted.
		final Set<String> deletedIds = new HashSet<>(ids);
		deletedIds.removeAll(updatedIds);
		deletedIds.removeAll(ignoredIds);
		
		for(String id : deletedIds) {
			boolean wasPresentInCache = cache.removeId(id); 
			if(!wasPresentInCache) {
			    //Only report as deleted what was actually already in the cache
				deletedIds.remove(id);
			}
		}
		
		UpdateCsvCacheResult result = new UpdateCsvCacheResult(updatedIds, deletedIds);
		if(!updatedIds.isEmpty() || !deletedIds.isEmpty()) {
			updateOutboundHub.sendUpdate(entityId, cache, result);
		} else {
			logger.info("No entities to update. Not sending an outbound message.");
		}
		return result;
	}
	
	public UpdateCsvCacheResult updateCsvCacheForGroup(final String entityId, final String group, final String idWithinGroup) {
	    throw new UnsupportedOperationException("Not yet supported");
	    /*
	    final List<CsvLine> lines = csvDataSource.getCsvLinesForGroup(entityId, group, idWithinGroup);
        final Set<String> updatedIds = new HashSet<>();
        final Set<String> ignoredIds = new HashSet<>();
        CsvCache cache = caches.get(entityId);
        */
	}
	
	public static class UpdateCsvCacheResult {
		private Set<String> updatedIds;
		private Set<String> deletedIds;
		
		public UpdateCsvCacheResult(Set<String> updatedIds, Set<String> deletedIds) {
			this.updatedIds = updatedIds;
			this.deletedIds = deletedIds;
		}
		
		public Set<String> getUpdatedIds() {
			return updatedIds;
		}
		
		public Set<String> getDeletedIds() {
			return deletedIds;
		}
	}
}