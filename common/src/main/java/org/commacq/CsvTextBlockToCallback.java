package org.commacq;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.Validate;

@Slf4j
public class CsvTextBlockToCallback {
	
	public String getCsvColumnNames(Reader text) {
		CSVParser parser = new CSVParser(text);
		String[] header;
		try {
			header = parser.getLine();
		} catch (IOException ex) {
			throw new RuntimeException("Error parsing CSV", ex);
		}
		
		StringWriter stringWriter = new StringWriter();
		CSVPrinter printer = new CSVPrinter(stringWriter);
		
		printer.println(header);
		String headerString = stringWriter.toString();
		//Remove newline characters.
		headerString = headerString.substring(0, headerString.length() - System.lineSeparator().length());
		
		return headerString;
	}
	
	public void presentTextBlockToCsvLineCallback(String text, CsvLineCallback callback, boolean callStartAndFinish) {

		CSVParser parser = new CSVParser(new StringReader(text));
		
		String[] header;
		try {
			header = parser.getLine();
		} catch (IOException ex) {
			throw new RuntimeException("Error parsing CSV", ex);
		}
		
        Validate.notEmpty(header, "At least the header row is required in the CSV text");
        String firstColumnHeader = header[0];
        Validate.notEmpty(firstColumnHeader, "The CSV header row must contain at least one column");
		
		StringWriter stringWriter = new StringWriter();
		CSVPrinter printer = new CSVPrinter(stringWriter);
		
		printer.println(header);
		
		String columnNamesCsv = stringWriter.toString();
		//Remove newline characters.
		columnNamesCsv = columnNamesCsv.substring(0, columnNamesCsv.length() - System.lineSeparator().length());
		
		try {
			if(callStartAndFinish) {
				callback.startUpdateBlock(columnNamesCsv);
			}
			
			stringWriter.getBuffer().setLength(0); //Clear buffer
			
			String[] csv;
			try {
				while((csv = parser.getLine()) != null) {
					printer.println(csv);
					String csvLine = stringWriter.toString();
					
					//Remove newline characters.
					csvLine = csvLine.substring(0, csvLine.length() - System.lineSeparator().length());
					
					stringWriter.getBuffer().setLength(0); //Clear buffer
					if(firstColumnHeader.equals("id")) {
						if(csv.length > 1) {
							callback.processUpdate(columnNamesCsv, new CsvLine(csv[0], csvLine));				
						} else {
							callback.processRemove(csv[0]);
						}
					} else {
						callback.startBulkUpdateForGroup(firstColumnHeader, csv[0]);
						//TODO the CsvDataSource should query all members of the group and push them to their subscribers.
						throw new RuntimeException("Update by group not yet supported");
					}
				}
			} catch (IOException ex) {
				throw new RuntimeException("Error parsing CSV", ex);
			}
			
			if(callStartAndFinish) {
				callback.finishUpdateBlock();
			}
		} catch(CsvUpdateBlockException ex) {
			log.error("Error processing callback", ex);
		}
	}
	
}
