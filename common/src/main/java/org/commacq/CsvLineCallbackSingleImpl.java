package org.commacq;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class CsvLineCallbackSingleImpl extends CsvLineCallbackAbstractSimple {

	private CsvLine csvLine;
	
	@Override
	public void processUpdate(String columnNamesCsv, CsvLine csvLine) throws CsvUpdateBlockException {
		this.csvLine = csvLine;
	}
	
	@Override
	public void processRemove(String id) throws CsvUpdateBlockException {
		csvLine = new CsvLine(id, null);
	}
	
	/**
	 * Fetches the CsvLine that was most recently set and then
	 * clears down the value to be null so that it can't be fetched
	 * twice.
	 * 
	 * Returns a CsvLine with just an id and null value if it was a
	 * remove operation.
	 */
	public CsvLine getCsvLineAndClear() {
		CsvLine returnMe = csvLine;
		csvLine = null;
		return returnMe;	
	}
	
}
