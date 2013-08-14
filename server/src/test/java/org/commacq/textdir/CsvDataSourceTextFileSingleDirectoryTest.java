package org.commacq.textdir;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.commacq.CsvCache;
import org.junit.Test;

public class CsvDataSourceTextFileSingleDirectoryTest {

	@Test
	public void test() {
		CsvDataSourceTextFileSingleDirectory source = new CsvDataSourceTextFileSingleDirectory(
				"testEntity",
				"classpath:/org/commacq/textdir/CsvDataSourceTextFileSingleDirectoryTestFiles"
		);
		
		CsvCache csvCache = source.createInitialCache("testEntity");
		
		assertEquals("id1,Contents of id1", csvCache.getLine("id1"));
		
		assertEquals("Quoted because of the comma in the text",
				     "id2,\"Contents of id2, which contains a comma.\"",
				     csvCache.getLine("id2"));
		
		
		assertEquals("Quoted id and empty text",
				     "\"id3,filename,with,commas\",",
				     csvCache.getLine("id3,filename,with,commas"));
		
		assertNull(csvCache.getLine("madeUpIdentifier"));
	}

}
