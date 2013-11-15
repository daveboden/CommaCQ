package org.commacq.layer;

import java.util.Collection;

import org.commacq.CsvLineCallback;

/**
 * Each Layer maintains a list of observers used
 * for real-time updates.
 */
public interface SubscribeLayer extends Layer {
	void getAllCsvLinesAndSubscribe(String entityId, CsvLineCallback callback);
	void getAllCsvLinesAndSubscribe(Collection<String> entityIds, CsvLineCallback callback);
    void getAllCsvLinesAndSubscribe(CsvLineCallback callback);
    void subscribe(String entityId, CsvLineCallback callback);
    void subscribe(Collection<String> entityIds, CsvLineCallback callback);
    void subscribe(CsvLineCallback callback);
    void unsubscribe(CsvLineCallback callback);
}
