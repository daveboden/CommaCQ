package org.commacq;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

public interface CsvDataSourceLayer {

	SortedSet<String> getEntityIds();
	
	String getCsvEntry(String entityId, String id);

	CsvDataSource getCsvDataSource(String entityId);

	Map<String, ? extends CsvDataSource> getMap();
	
    /**
     * Each Layer maintains a list of observers used
     * for real-time updates.
     */
	void getAllCsvLinesAndSubscribe(CsvLineCallback callback, String entityId);
	void getAllCsvLinesAndSubscribe(CsvLineCallback callback, List<String> entityIds);
    void getAllCsvLinesAndSubscribe(CsvLineCallback callback);
    void subscribe(CsvLineCallback callback, String entityId);
    void subscribe(CsvLineCallback callback, List<String> entityIds);
    void subscribe(CsvLineCallback callback);
    void unsubscribe(CsvLineCallback callback);
	
}