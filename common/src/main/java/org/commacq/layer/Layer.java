package org.commacq.layer;

import java.util.Collection;
import java.util.SortedSet;

import org.commacq.LineCallback;

public interface Layer {

	SortedSet<String> getEntityIds();
	String getColumnNamesCsv(String entityId);
	
	void getAllCsvLines(LineCallback callback);
	void getAllCsvLines(Collection<String> entityIds, LineCallback callback);
	
    void getAllCsvLines(String entityId, LineCallback callback);
    void getCsvLines(String entityId, Collection<String> ids, LineCallback callback);
    void getCsvLine(String entityId, String id, LineCallback callback);
    void getCsvLinesForGroup(String entityId, String group, String idWithinGroup, LineCallback callback);
	
}