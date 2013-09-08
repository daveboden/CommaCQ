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
	public void startUpdateBlock(String columnNamesCsv) {
		lock.lock();
		for(CsvLineCallback callback : callbacks) {
			callback.startUpdateBlock(columnNamesCsv);
		}
	}
	
	/**
	 * Finishing an update block causes the lock to be released.
	 * No subscribers can be added or removed half way through a block.
	 */
	@Override
	public void finishUpdateBlock() {
		for(CsvLineCallback callback : callbacks) {
			callback.finishUpdateBlock();
		}
	}
	
	@Override
	public void processRemove(String id) {
		for(CsvLineCallback callback : callbacks) {
			callback.processRemove(id);
		}
	}
	
	@Override
	public void processUpdate(String columnNamesCsv, CsvLine csvLine) {
		for(CsvLineCallback callback : callbacks) {
			callback.processUpdate(columnNamesCsv, csvLine);
		}			
	}
	
	@Override
	public void startBulkUpdate(String columnNamesCsv) {
		for(CsvLineCallback callback : callbacks) {
			callback.startBulkUpdate(columnNamesCsv);
		}
	}
	
	@Override
	public void startBulkUpdateForGroup(String group, String idWithinGroup) {
		for(CsvLineCallback callback : callbacks) {
			callback.startBulkUpdateForGroup(group, idWithinGroup);
		}
	}
	
	@Override
	public String toString() {
		return "CsvLineCallbackComposite - " + callbacks;
	}
}
