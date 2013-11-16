package org.commacq.layer;

import java.util.Collection;

import org.commacq.BlockCallback;

/**
 * Each Layer maintains a list of observers used
 * for real-time updates.
 */
public interface SubscribeLayer extends Layer {
	void getAllCsvLinesAndSubscribe(String entityId, BlockCallback callback);
	void getAllCsvLinesAndSubscribe(Collection<String> entityIds, BlockCallback callback);
    void getAllCsvLinesAndSubscribe(BlockCallback callback);
    void subscribe(String entityId, BlockCallback callback);
    void subscribe(Collection<String> entityIds, BlockCallback callback);
    void subscribe(BlockCallback callback);
    void unsubscribe(BlockCallback callback);
}
