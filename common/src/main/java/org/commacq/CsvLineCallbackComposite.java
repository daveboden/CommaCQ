package org.commacq;

import java.util.ArrayList;
import java.util.List;
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
	
	private final List<CsvLineCallback> callbacks = new ArrayList<>();
	
	public void addCallback(CsvLineCallback callback) {
		lock.lock();
		try {
			callbacks.add(callback);
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
		lock.lock();
		for(CsvLineCallback callback : callbacks) {
			callback.startUpdateBlock(entityId, csvColumnNames);
		}
	}
	
	/**
	 * Finishing an update block causes the lock to be released.
	 * No subscribers can be added or removed half way through a block.
	 */
	@Override
	public void finishUpdateBlock() throws CsvUpdateBlockException {
		for(CsvLineCallback callback : callbacks) {
			callback.finishUpdateBlock();
		}
		lock.unlock();
	}
	
	/**
	 * Cancelling an update block causes the lock to be released.
	 * No subscribers can be added or removed half way through a block.
	 */
	@Override
	public void cancel() {
		for(CsvLineCallback callback : callbacks) {
			callback.cancel();
		}
		lock.unlock();
	}
	
	@Override
	public void processRemove(String entityId, String id) throws CsvUpdateBlockException {
		for(CsvLineCallback callback : callbacks) {
			callback.processRemove(entityId, id);
		}
	}
	
	@Override
	public void processUpdate(String entityId, String columnNamesCsv, CsvLine csvLine) throws CsvUpdateBlockException {
		for(CsvLineCallback callback : callbacks) {
			callback.processUpdate(entityId, columnNamesCsv, csvLine);
		}			
	}
	
	@Override
	public void startBulkUpdate(String entityId, String columnNamesCsv) throws CsvUpdateBlockException {
		for(CsvLineCallback callback : callbacks) {
			callback.startBulkUpdate(entityId, columnNamesCsv);
		}
	}
	
	@Override
	public void startBulkUpdateForGroup(String entityId, String group, String idWithinGroup) throws CsvUpdateBlockException {
		for(CsvLineCallback callback : callbacks) {
			callback.startBulkUpdateForGroup(entityId, group, idWithinGroup);
		}
	}
	
	@Override
	public String toString() {
		return "CsvLineCallbackComposite - " + callbacks;
	}
}
