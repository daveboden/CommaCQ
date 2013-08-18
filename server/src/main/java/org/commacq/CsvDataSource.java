package org.commacq;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.commacq.CsvMarshaller.CsvLine;

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

    SortedSet<String> getEntityIds();
    Map<String, CsvCache> createInitialCaches();
    CsvCache createInitialCache(String entityId);
    List<CsvLine> getCsvLines(String entityId, Collection<String> ids);
    CsvLine getCsvLine(String entityId, String id);
    List<CsvLine> getCsvLinesForGroup(String entityId, String group, String idWithinGroup);
    
}