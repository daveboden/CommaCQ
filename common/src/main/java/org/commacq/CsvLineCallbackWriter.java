package org.commacq;

import java.io.PrintWriter;
import java.io.Writer;

/**
 * Contains a large buffer. Reuse these objects.
 */
public class CsvLineCallbackWriter implements CsvLineCallback {
	
	private final PrintWriter printWriter;
	
	public CsvLineCallbackWriter(Writer writer) {
		printWriter = new PrintWriter(writer);
	}
	
	@Override
	public void processUpdate(String entityId, String columnNamesCsv, CsvLine csvLine) throws CsvUpdateBlockException {
		printWriter.println(csvLine.getCsvLine());
	}
	
	@Override
	public void processRemove(String entityId, String id) throws CsvUpdateBlockException {
		printWriter.println(id);
	}
	
	/**
	 * Add csv header
	 */
	@Override
	public void startUpdateBlock(String entityId, String columnNamesCsv) throws CsvUpdateBlockException {
		printWriter.println(columnNamesCsv);
	}
	
	@Override
	public void start() throws CsvUpdateBlockException {		
		//No behaviour defined.
	}
	
	@Override
	public final void finish() throws CsvUpdateBlockException {
		//No behaviour defined.
	}
	
	@Override
	public final void startBulkUpdate(String entityId, String columnNamesCsv) throws CsvUpdateBlockException {
		//No behaviour defined.
	}
	
	@Override
	public final void startBulkUpdateForGroup(String entityId, String group, String idWithinGroup) throws CsvUpdateBlockException {
		//No behaviour defined.
	}
	
	@Override
	public void cancel() {
		//No behaviour defined.		
	}
}
