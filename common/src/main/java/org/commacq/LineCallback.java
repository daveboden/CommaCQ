package org.commacq;

public interface LineCallback {
	
	void processUpdate(String entityId, String columnNamesCsv, CsvLine csvLine) throws CsvUpdateBlockException;
	void processRemove(String entityId, String columnNamesCsv, String id) throws CsvUpdateBlockException;
	
}
