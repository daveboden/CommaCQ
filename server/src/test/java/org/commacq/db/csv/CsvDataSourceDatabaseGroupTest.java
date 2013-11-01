package org.commacq.db.csv;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2;

import java.util.Set;

import org.commacq.CsvLineCallback;
import org.commacq.CsvUpdateBlockException;
import org.commacq.db.DataSourceAccess;
import org.commacq.db.EntityConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import com.google.common.collect.ImmutableSet;

@RunWith(MockitoJUnitRunner.class)
public class CsvDataSourceDatabaseGroupTest {
	
	@Mock
	private CsvLineCallback callback;
	
	private EmbeddedDatabase dataSource;
	private CsvDataSourceDatabase csvDataSourceDatabase;
	
	@Before
	public void setupDataSource() {
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		dataSource = builder.setType(H2).addScript("classpath:/org/commacq/db/csv/test-group.sql").build();
		
		DataSourceAccess dataSourceAccess = new DataSourceAccess(dataSource);
		Set<String> groupNames = ImmutableSet.of("groupName", "superGroupName");
		EntityConfig entityConfig = new EntityConfig("test",
				                                     "select \"id\", \"groupName\", \"superGroupName\", \"value\" from TestTable",
				                                     groupNames);
		csvDataSourceDatabase = new CsvDataSourceDatabase(dataSourceAccess, entityConfig); 
	}
	
	@After
	public void tearDownDataSource() {
		dataSource.shutdown();
	}
	
	@Test
	public void testBadGroupCausesError() throws CsvUpdateBlockException {
		DataSourceAccess dataSourceAccess = new DataSourceAccess(dataSource);
		Set<String> badGroupNames = ImmutableSet.of("badGroupName");
		EntityConfig entityConfig = new EntityConfig("test",
				                                     "select \"id\", \"groupName\", \"superGroupName\", \"value\" from TestTable",
				                                     badGroupNames);
		CsvDataSourceDatabase badCsvDataSourceDatabase = new CsvDataSourceDatabase(dataSourceAccess, entityConfig);
		try {
			badCsvDataSourceDatabase.getAllCsvLines(callback);
			fail("Expected DataAccessException");
		} catch(DataAccessException ex) {
			//Expected exception
		}
		
		verify(callback).cancel();
	}
	
	@Ignore //Group updates not yet handled.
	@Test
	public void testAllCsvLinesAndSubscribe() {
		/*
		csvDataSourceDatabase.subscribe(callback);
		csvDataSourceDatabase.startBulkUpdateForGroup(("id,groupName,superGroupName,value", "groupName", "group1");
		verify(callback).startBulkUpdateForGroup("groupName");
		verify(callback, times(2)).processUpdate(anyString(), any(CsvLine.class));
		verify(callback).finishUpdateBlock();
		verifyNoMoreInteractions(callback);
		*/
	}
}
