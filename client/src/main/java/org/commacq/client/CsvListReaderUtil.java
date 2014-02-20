package org.commacq.client;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;

import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

public class CsvListReaderUtil {

    private final PipedWriter parserWriter;
    private final PipedReader parserReader;
    private final CsvListReader parser;
    
    public CsvListReaderUtil() {
    	parserWriter = new PipedWriter();
    	try {
    		//64k buffer
			parserReader = new PipedReader(parserWriter, 65536);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
    	parser = new CsvListReader(parserReader, CsvPreference.STANDARD_PREFERENCE);
	}
    
    public CsvListReader getParser() {
    	return parser;
    }
    
    public void appendLine(String line) throws IOException {
    	parserWriter.append(line).append("\n");
    }
	
}
