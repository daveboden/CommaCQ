package org.commacq;

import java.util.Collection;


/**
 * Callback that's touched every time a CsvLine is returned by a DataSource or Layer.
 * Encourages each line to be fully processed before moving onto the next one. This
 * avoids having to read all lines into memory before doing anything with them.
 * 
 * For the real-time update mechanism, the source of the updates is responsible for
 * serialising the updates into blocks and calling finish() after each block.
 * 
 * This callback can be seen as a replacement for a data structure like:
 * 
 * Entity 1:
 *   * Csv column headings
 *   * CsvLines updated
 *   * Ids removed
 *   * Flag to say whether the updates consist of an entire bulk update
 *   * One or more flags to say whether the updates entirely replace a grouping
 * Entity 2:
 *   * ...
 * 
 * @see CsvLineCallbackListImpl for a simple implementation that reads everything
 *                              into a list when there's no need to stream the data.
 */
public interface BlockCallback extends LineCallback {

	/*
	 * Signifies that a complete replacement of the underlying data
	 * is underway. Callback should respond by throwing away any
	 * identifiers not mentioned between the start and finish
	 * of a bulk update.
	 * 
	 * During a bulk update, even the column structure can change.
	 */
	void startBulkUpdate(String entityId, String columnNamesCsv) throws CsvUpdateBlockException;
	
	void startBulkUpdateForGroup(String entityId, String group, String idWithinGroup) throws CsvUpdateBlockException;
	
	void start(Collection<String> entityIds) throws CsvUpdateBlockException;
	
	/**
	 * Allows subscribers to behave transactionally. finish() is called after
	 * every block of updates, possibly across multiple entities.
	 * @throws CsvUpdateBlockException
	 */
	void finish() throws CsvUpdateBlockException;
	
	/**
	 * Provides a way of abandoning an update block part-way through.
	 */
	void cancel();
	
}
