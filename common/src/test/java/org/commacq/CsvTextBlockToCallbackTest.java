package org.commacq;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;

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
		BlockCallback callback = mock(BlockCallback.class);
		
		InputStreamReader file1Reader = new InputStreamReader(getClass().getResourceAsStream("/org/commacq/csvTextBlockToCallback1.csv"));
		assertEquals("id,name,alias", csvTextBlockToCallback.getCsvColumnNames(file1Reader));
		
		String file1 = IOUtils.toString(getClass().getResourceAsStream("/org/commacq/csvTextBlockToCallback1.csv"));
		
		final String entityId = "testEntity";

		csvTextBlockToCallback.presentTextBlockToCsvLineCallback(entityId, file1, callback);
		
		verify(callback).processUpdate(entityId, "id,name,alias", new CsvLine("1", "1,ABC,ABC1"));
		verify(callback).processUpdate(entityId, "id,name,alias", new CsvLine("2", "2,DEF,DEF2"));
		verify(callback).processRemove(entityId, "id,name,alias", "3");
		verify(callback).processRemove(entityId, "id,name,alias", "4");
		verify(callback).processUpdate(entityId, "id,name,alias", new CsvLine("5", "5,GHI,GHI9"));
		verifyNoMoreInteractions(callback);
	}

}
