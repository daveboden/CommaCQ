package org.commacq;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;

/**
 * Contains a large buffer. Reuse these objects.
 */
public class CsvLineCallbackWriter implements BlockCallback {
	
	private final PrintWriter printWriter;
	
	public CsvLineCallbackWriter(Writer writer, String columnNamesCsv) {
		printWriter = new PrintWriter(writer);
		printWriter.println(columnNamesCsv);
	}
	
	@Override
	public void processUpdate(String entityId, String columnNamesCsv, CsvLine csvLine) throws CsvUpdateBlockException {
		printWriter.println(csvLine.getCsvLine());
	}
	
	@Override
	public void processRemove(String entityId, String columnNamesCsv, String id) throws CsvUpdateBlockException {
		printWriter.println(id);
	}
	
	@Override
	public void start(Collection<String> entityIds) throws CsvUpdateBlockException {		
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
