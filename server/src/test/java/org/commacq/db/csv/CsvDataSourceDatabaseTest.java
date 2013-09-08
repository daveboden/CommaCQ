package org.commacq.db.csv;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.commacq.CsvLine;
import org.commacq.CsvLineCallback;
import org.commacq.db.DataSourceAccess;
import org.commacq.db.EntityConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

@RunWith(MockitoJUnitRunner.class)
public class CsvDataSourceDatabaseTest {
	
	@Mock
	private CsvLineCallback callback;
	
	private EmbeddedDatabase dataSource;
	private CsvDataSourceDatabase csvDataSourceDatabase;
	
	@Before
	public void setupDataSource() {
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		dataSource = builder.setType(H2).addScript("classpath:/org/commacq/db/csv/test.sql").build();
		
		DataSourceAccess dataSourceAccess = new DataSourceAccess(dataSource);
		EntityConfig entityConfig = new EntityConfig("test", "select \"id\", \"name\" from TestTable");
		csvDataSourceDatabase = new CsvDataSourceDatabase(dataSourceAccess, entityConfig); 
	}
	
	@After
	public void tearDownDataSource() {
		dataSource.shutdown();
	}
	
	@Test
	public void testEntityId() {
		assertEquals("test", csvDataSourceDatabase.getEntityId());
	}
	
	@Test
	public void testColumnNames() {
		assertEquals("id,name", csvDataSourceDatabase.getColumnNamesCsv());
	}
	
	@Test
	public void testAllCsvLinesAndSubscribe() {
		csvDataSourceDatabase.getAllCsvLinesAndSubscribe(callback);
		verify(callback, times(2)).processUpdate(anyString(), any(CsvLine.class));
		verifyNoMoreInteractions(callback);
	}
		
	@Test
	public void testUpdates() {
		csvDataSourceDatabase.subscribe(callback);
		
		csvDataSourceDatabase.processUpdate("id,name", new CsvLine("1", "1,ZZZ"));
		verify(callback).processUpdate("id,name", new CsvLine("1", "1,ZZZ"));
		verifyNoMoreInteractions(callback);
		reset(callback);

		csvDataSourceDatabase.updateUntrusted("1");
		//YYY update should be ignored, because the underlying data is still ABC.
		verify(callback).processUpdate("id,name", new CsvLine("1", "1,ABC"));
		verifyNoMoreInteractions(callback);
		reset(callback);
		
		csvDataSourceDatabase.startBulkUpdate("id,name");
		verify(callback).startBulkUpdate(anyString());
		verify(callback, times(2)).processUpdate(anyString(), any(CsvLine.class));
		verifyNoMoreInteractions(callback);
		reset(callback);
		
		List<String> bothLines = new ArrayList<>();
		bothLines.add("1");
		bothLines.add("2");
		csvDataSourceDatabase.updateUntrusted(bothLines);
		verify(callback, times(2)).processUpdate(anyString(), any(CsvLine.class));
		verifyNoMoreInteractions(callback);
		reset(callback);
		
		//Remove line by setting CsvLine to null
		csvDataSourceDatabase.processRemove("1");
		verify(callback).processRemove("1");
		verifyNoMoreInteractions(callback);
		reset(callback);

		csvDataSourceDatabase.updateUntrusted("1");
		//We haven't really removed the underlying data, so remove should not be called
		//in reconcile mode. An error should be logged.
		verify(callback, times(0)).processRemove("1");
		verify(callback).processUpdate("id,name", new CsvLine("1", "1,ABC"));
		verifyNoMoreInteractions(callback);
		reset(callback);
	}
	
	@Test
	public void testUpdateForEntryThatHasBeenRemovedFromDatabase() throws SQLException {
		csvDataSourceDatabase.subscribe(callback);
		
		dataSource.getConnection().prepareStatement("delete from TestTable where \"id\"='1'").executeUpdate();
		//Specify a line update for a line which no longer exists in the data source - use reconcile mode.
		//The line should be removed. An error should be logged.
		csvDataSourceDatabase.updateUntrusted("1");
		verify(callback).processRemove("1");
		verifyNoMoreInteractions(callback);
		reset(callback);
	}
}
