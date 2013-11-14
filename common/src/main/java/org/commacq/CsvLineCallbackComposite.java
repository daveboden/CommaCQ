package org.commacq;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Composite callback that calls back each of the observers in turn.
 */
public class CsvLineCallbackComposite implements CsvLineCallback {

	/**
	 * Lock ensures that a callback can't be added or removed half
	 * way through an update batch.
	 */
	private final ReentrantLock lock = new ReentrantLock();
	private Set<CsvLineCallback> calledInThisTransaction;
	
	/**
	 * Maintains active callbacks along with a list of entities that each
	 * callback is interested in.
	 * null means all entities.
	 */
	private final Map<CsvLineCallback, List<String>> callbacks = new HashMap<>();
	
	public void addCallback(CsvLineCallback callback) {
		addCallback(callback, (List<String>)null);
	}
	
	public void addCallback(CsvLineCallback callback, List<String> entityIds) {
		lock.lock();
		try {
			callbacks.put(callback, entityIds);
		} finally {
			lock.unlock();
		}
	}
	
	public void addCallback(CsvLineCallback callback, String entityId) {
		lock.lock();
		try {
			callbacks.put(callback, Collections.singletonList(entityId));
		} finally {
			lock.unlock();
		}
	}
	
	public void removeCallback(CsvLineCallback callback) {
		lock.lock();
		try {
			callbacks.remove(callback);
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * Starting an update block causes the lock to be held.
	 * No subscribers can be added or removed half way through a block.
	 */
	@Override
	public void startUpdateBlock(String entityId, String csvColumnNames) throws CsvUpdateBlockException {
		for(Entry<CsvLineCallback, List<String>> entry : callbacks.entrySet()) {
			List<String> entityIds = entry.getValue();
			if(entityIds == null || entityIds.contains(entityId)) {
				CsvLineCallback callback = entry.getKey();
				calledInThisTransaction.add(callback);
				callback.startUpdateBlock(entityId, csvColumnNames);
			}
		}
	}
	
	@Override
	public void start() throws CsvUpdateBlockException {
		lock.lock();		
	}
	
	/**
	 * Finishing an update block causes the lock to be released.
	 * No subscribers can be added or removed half way through a block.
	 */
	@Override
	public void finish() throws CsvUpdateBlockException {
		for(CsvLineCallback callback : calledInThisTransaction) {
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
		for(CsvLineCallback callback : calledInThisTransaction) {
			callback.cancel();
		}
		calledInThisTransaction.clear();
		lock.unlock();
	}
	
	@Override
	public void processRemove(String entityId, String id) throws CsvUpdateBlockException {
		for(Entry<CsvLineCallback, List<String>> entry : callbacks.entrySet()) {
			List<String> entityIds = entry.getValue();
			if(entityIds == null || entityIds.contains(entityId)) {
				CsvLineCallback callback = entry.getKey();
				callback.processRemove(entityId, id);
			}
		}
	}
	
	@Override
	public void processUpdate(String entityId, String columnNamesCsv, CsvLine csvLine) throws CsvUpdateBlockException {
		for(Entry<CsvLineCallback, List<String>> entry : callbacks.entrySet()) {
			List<String> entityIds = entry.getValue();
			if(entityIds == null || entityIds.contains(entityId)) {
				CsvLineCallback callback = entry.getKey();
				callback.processUpdate(entityId, columnNamesCsv, csvLine);
			}
		}			
	}
	
	@Override
	public void startBulkUpdate(String entityId, String columnNamesCsv) throws CsvUpdateBlockException {
		for(Entry<CsvLineCallback, List<String>> entry : callbacks.entrySet()) {
			List<String> entityIds = entry.getValue();
			if(entityIds == null || entityIds.contains(entityId)) {
				CsvLineCallback callback = entry.getKey();
				callback.startBulkUpdate(entityId, columnNamesCsv);
			}
		}
	}
	
	@Override
	public void startBulkUpdateForGroup(String entityId, String group, String idWithinGroup) throws CsvUpdateBlockException {
		for(Entry<CsvLineCallback, List<String>> entry : callbacks.entrySet()) {
			List<String> entityIds = entry.getValue();
			if(entityIds == null || entityIds.contains(entityId)) {
				CsvLineCallback callback = entry.getKey();
				callback.startBulkUpdateForGroup(entityId, group, idWithinGroup);
			}
		}
	}
	
	@Override
	public String toString() {
		return "CsvLineCallbackComposite - " + callbacks;
	}
}
