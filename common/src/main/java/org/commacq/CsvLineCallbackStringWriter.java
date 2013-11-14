package org.commacq;

import java.io.PrintWriter;
import java.io.StringWriter;

import lombok.Getter;

/**
 * Contains a large buffer. Reuse these objects.
 */
public class CsvLineCallbackStringWriter implements CsvLineCallback {
	
	private StringWriter writer = new StringWriter(1_000_000);
	private PrintWriter printWriter = new PrintWriter(writer);
	@Getter
	private int processUpdateCount = 0;
	@Getter
	private int processRemoveCount = 0;
	
	@Override
	public void startUpdateBlock(String entityId, String columnNamesCsv) throws CsvUpdateBlockException {
		printWriter.println(columnNamesCsv);
	}
	
	@Override
	public void processUpdate(String entityId, String columnNamesCsv, CsvLine csvLine) throws CsvUpdateBlockException {
		printWriter.println(csvLine.getCsvLine());
		processUpdateCount++;
	}
	
	@Override
	public void processRemove(String entityId, String id) throws CsvUpdateBlockException {
		printWriter.println(id);
		processRemoveCount++;
	}
	
	public void clear() {
		writer.getBuffer().setLength(0);
		processUpdateCount = 0;
		processRemoveCount = 0;
	}
	
	@Override
	public String toString() {
		return writer.toString();
	}
	
	@Override
	public void start() throws CsvUpdateBlockException {		
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
