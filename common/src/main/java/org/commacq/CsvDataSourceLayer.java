package org.commacq;

import java.util.Map;
import java.util.SortedSet;

public interface CsvDataSourceLayer {

	SortedSet<String> getEntityIds();
	
	String getCsvEntry(String entityId, String id);

	String pokeCsvEntry(String entityId, String id) throws CsvUpdateBlockException;

	CsvDataSource getCsvDataSource(String entityId);

	Map<String, CsvDataSource> getMap();

	void reload(String entityId) throws CsvUpdateBlockException;
	void reloadAll() throws CsvUpdateBlockException;
	
    /**
     * Each Layer maintains a list of observers used
     * for real-time updates.
     */
	void getAllCsvLinesAndSubscribe(String entityId, CsvLineCallback callback);
    void getAllCsvLinesAndSubscribe(CsvLineCallback callback);
    void subscribe(CsvLineCallback callback);
    void unsubscribe(CsvLineCallback callback);
	
}