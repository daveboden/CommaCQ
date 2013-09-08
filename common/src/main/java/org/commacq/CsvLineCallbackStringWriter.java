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
	public void startUpdateBlock(String columnNamesCsv) {
		printWriter.println(columnNamesCsv);
	}
	
	@Override
	public void processUpdate(String columnNamesCsv, CsvLine csvLine) {
		printWriter.println(csvLine.getCsvLine());
		processUpdateCount++;
	}
	
	@Override
	public void processRemove(String id) {
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
	public void finishUpdateBlock() {	
	}
	
	@Override
	public void startBulkUpdate(String columnNamesCsv) {	
	}
	
	@Override
	public void startBulkUpdateForGroup(String group, String idWithinGroup) {	
	}
	
}
