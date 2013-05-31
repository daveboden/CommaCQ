package org.commacq;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.lang3.Validate;

import org.commacq.CsvParser.CsvLine;

/**
 * Contains a linked list of CSV lines that can be traversed and written
 * to an output stream quickly in order.
 * 
 * The first column must be labelled "id".
 * 
 * Keeps track of the header fields and makes sure the fields get added
 * in the correct order on each line.
 */
@ThreadSafe
public final class CsvCache {

	private final SortedMap<String, String> linesIdMap = new TreeMap<>();
	private final String columnNamesCsv;
	
	public CsvCache(final String columnNamesCsv) {
		Validate.notEmpty(columnNamesCsv);
		Validate.isTrue(columnNamesCsv.startsWith("id,"), "id must be the first specified column: %s", columnNamesCsv);
		
		this.columnNamesCsv = columnNamesCsv; 
	}
	
	/**
	 * The result set must have the columns defined in the same
	 * order as in the header. This is not checked.
	 * 
	 * @return previous value that was in the cache or null if this is a new entry
	 */
	public String updateLine(final CsvLine csvLine) {
		synchronized(linesIdMap) {
			return linesIdMap.put(csvLine.getId(), csvLine.getCsvLine());
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
	
	public void writeToOutput(final Writer writer) throws IOException {
		writer.append(columnNamesCsv);
		synchronized(linesIdMap) {
			for(String line : linesIdMap.values()) {
				writer.append("\r\n");
				writer.append(line);
			}
		}
	}
	
	/**
	 * Outputs a subset of the total cache. Maintains the cache ordering regardless of the
	 * ordering of the ids passed in.
	 * @param writer
	 * @param ids Does not modify list in place; takes a defensive copy first then sorts the copy
	 * @throws IOException
	 */
	public void writeToOutput(final Writer writer, final Collection<String> ids) throws IOException {
		final List<String> sortedIds = new ArrayList<>(ids);
		Collections.sort(sortedIds);
		writer.append(columnNamesCsv);
		synchronized(linesIdMap) {
			for(String id : sortedIds) {
				writer.append("\r\n");
				writer.append(linesIdMap.get(id));
			}
		}
	}
	
	public String getLine(final String id) {
	    return linesIdMap.get(id);
	}
}