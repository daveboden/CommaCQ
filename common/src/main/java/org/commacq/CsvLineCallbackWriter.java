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
	public void processUpdate(String columnNamesCsv, CsvLine csvLine) throws CsvUpdateBlockException {
		printWriter.println(csvLine.getCsvLine());
	}
	
	@Override
	public void processRemove(String id) throws CsvUpdateBlockException {
		printWriter.println(id);
	}
	
	/**
	 * Add csv header
	 */
	@Override
	public void startUpdateBlock(String columnNamesCsv) throws CsvUpdateBlockException {
		printWriter.println(columnNamesCsv);
	}
	
	@Override
	public final void finishUpdateBlock() throws CsvUpdateBlockException {
		//No behaviour defined.
	}
	
	@Override
	public final void startBulkUpdate(String columnNamesCsv) throws CsvUpdateBlockException {
		//No behaviour defined.
	}
	
	@Override
	public final void startBulkUpdateForGroup(String group, String idWithinGroup) throws CsvUpdateBlockException {
		//No behaviour defined.
	}
	
	@Override
	public void cancel() {
		//No behaviour defined.		
	}
}
