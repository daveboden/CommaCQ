package org.commacq;

import java.util.Collection;

/**
 * Represents an endpoint that CSV data can be obtained from.
 * This is either a database, a file or another CommaCQ server
 * which is acting as a proxy for the data source, allowing
 * servers to be chained together across regions.
 * 
 * The source is also responsible for specifying which entity
 * names it has access to.
 */
public interface CsvDataSource {

    String getEntityId();
    String getColumnNamesCsv();
    
    void getAllCsvLines(CsvLineCallback callback);
    /**
	 * Outputs a subset of the total cache. Maintains the cache ordering regardless of the
	 * ordering of the ids passed in.
	 * 
	 * TODO Ensure that the ordering is respected
	 */
    void getCsvLines(Collection<String> ids, CsvLineCallback callback);
    void getCsvLine(String id, CsvLineCallback callback);
    void getCsvLinesForGroup(String group, String idWithinGroup, CsvLineCallback callback);

}