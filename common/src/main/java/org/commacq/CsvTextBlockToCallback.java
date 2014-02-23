package org.commacq;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.Validate;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

@Slf4j
public class CsvTextBlockToCallback {
	
	public String getCsvColumnNames(Reader text) {
		CsvListReader parser = new CsvListReader(text, CsvPreference.STANDARD_PREFERENCE);
		//Try-with-resources when upgrading to Java 7
		try {
			parser.read();
			return parser.getUntokenizedRow();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} finally {
			try {
				parser.close();
			} catch(IOException ex) {
			}
		}
	}
	
	public void presentTextBlockToCsvLineCallback(String entityId, String text, LineCallback callback) {
		presentTextBlockToCsvLineCallback(entityId, new StringReader(text), callback);
	}
	
	public void presentTextBlockToCsvLineCallback(String entityId, Reader textReader, LineCallback callback) {
		
		CsvListReader parser = new CsvListReader(textReader, CsvPreference.STANDARD_PREFERENCE);
		try {
			final String[] header = parser.getHeader(true);
			final String columnNamesCsv = parser.getUntokenizedRow();		
			Validate.notEmpty(columnNamesCsv, "At least the header row is required in the CSV text");
			final String firstColumnHeader = header[0];
			Validate.notEmpty(firstColumnHeader, "The CSV header row must contain at least one column");
			
			try {				
				List<String> csv;
				while((csv = parser.read()) != null) {
					String csvLine = parser.getUntokenizedRow();
					
					if(firstColumnHeader.equals("id")) {
						if(csv.size() > 1) {
							callback.processUpdate(entityId, columnNamesCsv, new CsvLine(csv.get(0), csvLine));				
						} else {
							callback.processRemove(entityId, columnNamesCsv, csv.get(0));
						}
					} else {
						//TODO the CsvDataSource should query all members of the group and push them to their subscribers.
						throw new RuntimeException("Update by group not yet supported");
					}
				}
			} catch(CsvUpdateBlockException ex) {
				log.error("Error processing callback", ex);
			}
			
		} catch (IOException ex) {
			throw new RuntimeException("Error parsing CSV", ex);
		} finally {
			try {
				parser.close();
			} catch (IOException ex) {
				log.warn("Error closing CSV parser", ex);
			}
		}
	}
	
}
