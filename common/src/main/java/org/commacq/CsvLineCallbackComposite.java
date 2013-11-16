package org.commacq;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Composite callback that calls back each of the observers in turn.
 */
public class CsvLineCallbackComposite implements BlockCallback {

	/**
	 * Lock ensures that a callback can't be added or removed half
	 * way through an update batch.
	 */
	private final ReentrantLock lock = new ReentrantLock();
	
	/**
	 * Maintains active callbacks along with a list of entities that each
	 * callback is interested in.
	 * null means all entities.
	 */
	private final Map<BlockCallback, Collection<String>> callbacks = new HashMap<>();
	
	public void addCallback(BlockCallback callback) {
		addCallback((Collection<String>)null, callback);
	}
	
	public void addCallback(Collection<String> entityIds, BlockCallback callback) {
		lock.lock();
		try {
			callbacks.put(callback, entityIds);
		} finally {
			lock.unlock();
		}
	}
	
	public void addCallback(String entityId, BlockCallback callback) {
		lock.lock();
		try {
			callbacks.put(callback, Collections.singletonList(entityId));
		} finally {
			lock.unlock();
		}
	}
	
	public void removeCallback(BlockCallback callback) {
		lock.lock();
		try {
			callbacks.remove(callback);
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * Starting an update causes the lock to be held.
	 * No subscribers can be added or removed half way through a block.
	 */
	@Override
	public void start(final Collection<String> entityIds) throws CsvUpdateBlockException {
		lock.lock();
		for(Entry<BlockCallback, Collection<String>> entry : callbacks.entrySet()) {
			entry.getKey().start(entityIds);
		}
	}
	
	/**
	 * Finishing an update block causes the lock to be released.
	 * No subscribers can be added or removed half way through a block.
	 */
	@Override
	public void finish() throws CsvUpdateBlockException {
		for(BlockCallback callback : callbacks.keySet()) {
			callback.finish();
		}
		lock.unlock();
	}
	
	/**
	 * Cancelling an update block causes the lock to be released.
	 * No subscribers can be added or removed half way through a block.
	 */
	@Override
	public void cancel() {
		for(BlockCallback callback : callbacks.keySet()) {
			callback.cancel();
		}
		lock.unlock();
	}
	
	@Override
	public void processRemove(String entityId, String columnNamesCsv, String id) throws CsvUpdateBlockException {
		for(Entry<BlockCallback, Collection<String>> entry : callbacks.entrySet()) {
			Collection<String> entityIds = entry.getValue();
			if(entityIds == null || entityIds.contains(entityId)) {
				BlockCallback callback = entry.getKey();
				callback.processRemove(entityId, columnNamesCsv, id);
			}
		}
	}
	
	@Override
	public void processUpdate(String entityId, String columnNamesCsv, CsvLine csvLine) throws CsvUpdateBlockException {
		for(Entry<BlockCallback, Collection<String>> entry : callbacks.entrySet()) {
			Collection<String> entityIds = entry.getValue();
			if(entityIds == null || entityIds.contains(entityId)) {
				BlockCallback callback = entry.getKey();
				callback.processUpdate(entityId, columnNamesCsv, csvLine);
			}
		}			
	}
	
	@Override
	public void startBulkUpdate(String entityId, String columnNamesCsv) throws CsvUpdateBlockException {
		for(Entry<BlockCallback, Collection<String>> entry : callbacks.entrySet()) {
			Collection<String> entityIds = entry.getValue();
			if(entityIds == null || entityIds.contains(entityId)) {
				BlockCallback callback = entry.getKey();
				callback.startBulkUpdate(entityId, columnNamesCsv);
			}
		}
	}
	
	@Override
	public void startBulkUpdateForGroup(String entityId, String group, String idWithinGroup) throws CsvUpdateBlockException {
		for(Entry<BlockCallback, Collection<String>> entry : callbacks.entrySet()) {
			Collection<String> entityIds = entry.getValue();
			if(entityIds == null || entityIds.contains(entityId)) {
				BlockCallback callback = entry.getKey();
				callback.startBulkUpdateForGroup(entityId, group, idWithinGroup);
			}
		}
	}
	
	@Override
	public String toString() {
		return "CsvLineCallbackComposite - " + callbacks;
	}
}
