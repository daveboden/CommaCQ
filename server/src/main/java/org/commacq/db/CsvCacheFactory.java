package org.commacq.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.commacq.CsvCache;
import org.commacq.CsvParser;
import org.commacq.CsvParser.CsvLine;

/**
 * Contains a linked list of CSV lines that can be traversed and written
 * to an output stream quickly in order.
 * 
 * The first column must be labelled "id".
 * 
 * Keeps track of the header fields and makes sure the fields get added
 * in the correct order on each line.
 */
public final class CsvCacheFactory {

	private final CsvParser csvParser = new CsvParser();
	
	public CsvCache createCsvCache(ResultSet result) throws SQLException {
		String columnNamesCsv = csvParser.getColumnLabelsAsCsvLine(result);
		
		CsvCache csvCache = new CsvCache(columnNamesCsv);

		while(result.next()) {
			CsvLine csvLine = csvParser.toCsvLine(result);
			csvCache.updateLine(csvLine);
		}
		
		return csvCache;
	}
	
}