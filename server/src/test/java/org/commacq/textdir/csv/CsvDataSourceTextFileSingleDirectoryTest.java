package org.commacq.textdir.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.commacq.CsvLineCallbackSingleImpl;
import org.commacq.textdir.csv.CsvDataSourceTextFileSingleDirectory;
import org.junit.Test;

public class CsvDataSourceTextFileSingleDirectoryTest {

	@Test
	public void test() {
		CsvDataSourceTextFileSingleDirectory source = new CsvDataSourceTextFileSingleDirectory(
				"testEntity",
				"classpath:/org/commacq/textdir/files/CsvDataSourceTextFileSingleDirectoryTestFiles"
		);
		
		CsvLineCallbackSingleImpl callback = new CsvLineCallbackSingleImpl();
		
		source.getCsvLine("id1", callback);
		assertEquals("id1,Contents of id1", callback.getCsvLineAndClear().getCsvLine());
		
		source.getCsvLine("id2", callback);
		assertEquals("Quoted because of the comma in the text",
				     "id2,\"Contents of id2, which contains a comma.\"",
				     callback.getCsvLineAndClear().getCsvLine());
		
		source.getCsvLine("id3,filename,with,commas", callback);
		assertEquals("Quoted id and empty text",
				     "\"id3,filename,with,commas\",",
				     callback.getCsvLineAndClear().getCsvLine());
		
		source.getCsvLine("madeUpIdentifier", callback);
		assertNull(callback.getCsvLineAndClear().getCsvLine());
	}

}
