package org.commacq;

import java.util.Collection;

/**
 * Represents an endpoint that CSV data can be obtained from.
 * This is either a database, a file or another CommaCQ server
 * which is acting as a proxy for the data source, allowing
 * servers to be chained together, perhaps across regions.
 * 
 * The source is also responsible for specifying which entity
 * name it has access to.
 */
public interface CsvDataSource {

    String getEntityId();
    String getColumnNamesCsv();
    
    /**
     * Pushes all lines through the callback using
     * just the processUpdate method.
     */
    void getAllCsvLines(LineCallback callback);

    
    /**
     * Pushes a single line through the callback using
     * just a single invocation of either processUpdate and processRemove
     * depending on whether the line is found in the data source.
     */
    void getCsvLine(String id, LineCallback callback);
    
    /**
     * Same behaviour as {@link #getCsvLine(String, BlockCallback)}  but
     * operates on multiple lines in a single method call.
     */
    void getCsvLines(Collection<String> ids, LineCallback callback);
    
    void getCsvLinesForGroup(String group, String idWithinGroup, LineCallback callback);

}