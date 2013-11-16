package org.commacq;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;

import lombok.Getter;

/**
 * Contains a large buffer. Reuse these objects.
 */
public class CsvLineCallbackStringWriter implements BlockCallback {
	
	private StringWriter writer = new StringWriter(1_000_000);
	private PrintWriter printWriter = new PrintWriter(writer);
	@Getter
	private int processUpdateCount = 0;
	@Getter
	private int processRemoveCount = 0;
	private boolean headerWritten = false;
	
	@Override
	public void start(Collection<String> entityIds) throws CsvUpdateBlockException {
	}
	
	@Override
	public void processUpdate(String entityId, String columnNamesCsv, CsvLine csvLine) throws CsvUpdateBlockException {
		printHeaderIfFirst(columnNamesCsv);
		printWriter.println(csvLine.getCsvLine());
		processUpdateCount++;
	}
	
	@Override
	public void processRemove(String entityId, String columnNamesCsv, String id) throws CsvUpdateBlockException {
		printHeaderIfFirst(columnNamesCsv);
		printWriter.println(id);
		processRemoveCount++;
	}
	
	private void printHeaderIfFirst(String columnNamesCsv) {
		if(!headerWritten) {
			printWriter.println(columnNamesCsv);
			headerWritten = true;
		}
	}
	
	public void clear() {
		writer.getBuffer().setLength(0);
		processUpdateCount = 0;
		processRemoveCount = 0;
		headerWritten = false;
	}
	
	@Override
	public String toString() {
		return writer.toString();
	}
	
	@Override
	public void finish() throws CsvUpdateBlockException {	
	}
	
	@Override
	public void cancel() {	
	}
	
	@Override
	public void startBulkUpdate(String entityId, String columnNamesCsv) throws CsvUpdateBlockException {	
	}
	
	@Override
	public void startBulkUpdateForGroup(String entityId, String group, String idWithinGroup) throws CsvUpdateBlockException {	
	}
	
}
