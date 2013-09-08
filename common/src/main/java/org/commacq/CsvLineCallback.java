package org.commacq;


/**
 * Callback that's touched every time a CsvLine is returned by a CsvDataSource.
 * Encourages each line to be fully processed before moving onto the next one. This
 * avoids having to read all lines into memory before doing anything with them.
 * 
 * For the real-time update mechanism, the source of the updates is responsible for
 * serialising the updates into blocks and calling finishUpdate() after each block.
 * 
 * @see CsvLineCallbackListImpl for a simple implementation that reads everything
 *                              into a list when there's no need to stream the data.
 */
public interface CsvLineCallback {

	/*
	 * Signifies that a complete replacement of the underlying data
	 * is underway. Callback should respond by throwing away any
	 * identifiers not mentioned between the start and finish
	 * of a bulk update.
	 * 
	 * During a bulk update, even the column structure can change.
	 */
	void startBulkUpdate(String columnNamesCsv);
	
	void startBulkUpdateForGroup(String group, String idWithinGroup);
	
	void startUpdateBlock(String columnNamesCsv);
	
	/**
	 * Allows subscribers to behave transactionally. finishUpdateBlock() is called after
	 * every block of updates.
	 */
	void finishUpdateBlock();
	
	void processUpdate(String columnNamesCsv, CsvLine csvLine);
	void processRemove(String id);
	
}
