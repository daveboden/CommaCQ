package org.commacq.cache.csv;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.lang3.Validate;
import org.commacq.CsvLine;
import org.commacq.CsvLineCallback;

/**
 * Contains a linked list of CSV lines that can be traversed and written
 * to an output stream quickly in order.
 * 
 * The first column must be labelled "id".
 * 
 * Keeps track of the header fields and makes sure the fields get added
 * in the correct order on each line.
 * 
 * Has the additional responsibility of maintaining a map for groups,
 * mapping each value within a group to a set of CsvLines.
 */
@ThreadSafe
public final class CsvCache {

	private final SortedMap<String, CsvLine> linesIdMap = new TreeMap<>();
	private final List<String> groups;
	private final Map<String, Map<String, CsvLine>> groupsMap = new HashMap<String, Map<String,CsvLine>>();
	private final String columnNamesCsv;
	
	public CsvCache(final String columnNamesCsv) {
	    this(columnNamesCsv, null);
	}
	
	public CsvCache(final String columnNamesCsv, final List<String> groups) {
		Validate.notEmpty(columnNamesCsv);
		Validate.isTrue(columnNamesCsv.startsWith("id,"), "id must be the first specified column: %s", columnNamesCsv);
		
		this.columnNamesCsv = columnNamesCsv;
		this.groups = groups;
	}
	
	public String getColumnNamesCsv() {
		return columnNamesCsv;
	}
	
	/**
	 * The result set must have the columns defined in the same
	 * order as in the header. This is not checked.
	 * 
	 * @return previous value that was in the cache or null if this is a new entry
	 */
	public CsvLine updateLine(final CsvLine csvLine) {
		synchronized(linesIdMap) {
			CsvLine previous = linesIdMap.put(csvLine.getId(), csvLine);
			return previous;
		}
	}
	
	public boolean removeId(String id) {
		synchronized(linesIdMap) {
			return linesIdMap.remove(id) != null;
		}
	}
	
	public int size() {
		return linesIdMap.size();
	}
		
	public CsvLine getLine(final String id) {
	    return linesIdMap.get(id);
	}
	
	public void visitAll(CsvLineCallback callback) {
		for(CsvLine csvLine : linesIdMap.values()) {
			callback.processUpdate(columnNamesCsv, csvLine);
		}
	}
	
	public void visitIds(CsvLineCallback callback, Collection<String> ids) {
		for(String id : ids) {
			CsvLine csvLine = linesIdMap.get(id);
			if(csvLine != null) {
				callback.processUpdate(columnNamesCsv, csvLine);
			}
		}
	}
	
	public void visitGroup(CsvLineCallback callback, String group, String idWithinGroup) {
		Map<String, CsvLine> groupContents = groupsMap.get(group);
		for(CsvLine csvLine : groupContents.values()) {
			callback.processUpdate(idWithinGroup, csvLine);
		}
	}

}