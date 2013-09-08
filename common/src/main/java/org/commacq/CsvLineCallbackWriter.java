package org.commacq;

import java.io.PrintWriter;
import java.io.Writer;

/**
 * Contains a large buffer. Reuse these objects.
 */
public class CsvLineCallbackWriter extends CsvLineCallbackAbstractSimple {
	
	private final PrintWriter printWriter;
	
	public CsvLineCallbackWriter(Writer writer) {
		printWriter = new PrintWriter(writer);
	}
	
	@Override
	public void processUpdate(String columnNamesCsv, CsvLine csvLine) {
		printWriter.println(csvLine.getCsvLine());
	}
	
	@Override
	public void processRemove(String id) {
		printWriter.println(id);
	}
}
