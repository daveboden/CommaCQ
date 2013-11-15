package org.commacq.layer;

import java.util.Collection;
import java.util.SortedSet;

import org.commacq.CsvLineCallback;

public interface Layer {

	SortedSet<String> getEntityIds();
	String getColumnNamesCsv(String entityId);
	
	void getAllCsvLines(CsvLineCallback callback);
	void getAllCsvLines(Collection<String> entityIds, CsvLineCallback callback);
	
    void getAllCsvLines(String entityId, CsvLineCallback callback);
    void getCsvLines(String entityId, Collection<String> ids, CsvLineCallback callback);
    void getCsvLine(String entityId, String id, CsvLineCallback callback);
    void getCsvLinesForGroup(String entityId, String group, String idWithinGroup, CsvLineCallback callback);
	
}