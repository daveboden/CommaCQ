package org.commacq;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class CsvTextBlockToCallbackTest {
	
	/*
id,name,alias
1,ABC,ABC1
2,DEF,DEF2
3
4
5,GHI,GHI9
	 */

	@Test
	public void test() throws IOException, CsvUpdateBlockException {
		CsvTextBlockToCallback csvTextBlockToCallback = new CsvTextBlockToCallback();
		CsvLineCallback callback = mock(CsvLineCallback.class);
		
		InputStreamReader file1Reader = new InputStreamReader(getClass().getResourceAsStream("/org/commacq/csvTextBlockToCallback1.csv"));
		assertEquals("id,name,alias", csvTextBlockToCallback.getCsvColumnNames(file1Reader));
		
		String file1 = IOUtils.toString(getClass().getResourceAsStream("/org/commacq/csvTextBlockToCallback1.csv"));
		
		csvTextBlockToCallback.presentTextBlockToCsvLineCallback(file1, callback, false);
		
		verify(callback).processUpdate("id,name,alias", new CsvLine("1", "1,ABC,ABC1"));
		verify(callback).processUpdate("id,name,alias", new CsvLine("2", "2,DEF,DEF2"));
		verify(callback).processRemove("3");
		verify(callback).processRemove("4");
		verify(callback).processUpdate("id,name,alias", new CsvLine("5", "5,GHI,GHI9"));
		verifyNoMoreInteractions(callback);
		
		reset(callback);
		
		csvTextBlockToCallback.presentTextBlockToCsvLineCallback(file1, callback, true);
		
		verify(callback).startUpdateBlock("id,name,alias");
		verify(callback).processUpdate("id,name,alias", new CsvLine("1", "1,ABC,ABC1"));
		verify(callback).processUpdate("id,name,alias", new CsvLine("2", "2,DEF,DEF2"));
		verify(callback).processRemove("3");
		verify(callback).processRemove("4");
		verify(callback).processUpdate("id,name,alias", new CsvLine("5", "5,GHI,GHI9"));
		verify(callback).finishUpdateBlock();
		verifyNoMoreInteractions(callback);
	}

}
