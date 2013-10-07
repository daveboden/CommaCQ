package org.commacq.cache.csv;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2;

import java.sql.SQLException;

import org.commacq.CsvLine;
import org.commacq.CsvLineCallback;
import org.commacq.CsvUpdateBlockException;
import org.commacq.db.DataSourceAccess;
import org.commacq.db.EntityConfig;
import org.commacq.db.csv.CsvDataSourceDatabase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

/**
 * Tests a cache on top of a database source. 
 */
@RunWith(MockitoJUnitRunner.class)
public class CsvDataSourceCacheTest {
		
	@Mock
	private CsvLineCallback callback;
	
	private EmbeddedDatabase dataSource;
	private CsvDataSourceDatabase csvDataSourceDatabase;
	private CsvDataSourceCache csvDataSourceCache;
	
	@Before
	public void setupDataSource() {
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		dataSource = builder.setType(H2).addScript("classpath:/org/commacq/db/csv/test.sql").build();
		
		DataSourceAccess dataSourceAccess = new DataSourceAccess(dataSource);
		EntityConfig entityConfig = new EntityConfig("test", "select \"id\", \"name\" from TestTable");
		csvDataSourceDatabase = new CsvDataSourceDatabase(dataSourceAccess, entityConfig); 
		csvDataSourceCache = new CsvDataSourceCache(csvDataSourceDatabase);
	}
	
	@After
	public void tearDownDataSource() {
		dataSource.shutdown();
	}
	
	@Test
	public void testSimpleRead() throws SQLException, CsvUpdateBlockException {
		
		csvDataSourceCache.getCsvLine("1", callback);
		verify(callback).processUpdate("id,name", new CsvLine("1", "1,ABC"));
		verifyNoMoreInteractions(callback);
		
	}
	
	@Test
	public void testUpdate() throws SQLException, CsvUpdateBlockException {
		dataSource.getConnection().prepareStatement("delete from TestTable where \"id\"='1'").executeUpdate();

		//Line is still returned by cache. Database change has not been propagated to cache.
		csvDataSourceCache.getCsvLine("1", callback);
		verify(callback).processUpdate("id,name", new CsvLine("1", "1,ABC"));
		verifyNoMoreInteractions(callback);
		reset(callback);
		
		//Propagate the database change to the cache.
		csvDataSourceDatabase.startUpdateBlock("id,name");
		csvDataSourceDatabase.startBulkUpdate("id,name");
		csvDataSourceDatabase.finishUpdateBlock();
		
		//Value is not present in the cache after refresh from the database
		//has propagated to the cache.
		csvDataSourceCache.getCsvLine("1", callback);
		verify(callback).processRemove("1");
		verifyNoMoreInteractions(callback);
		
	}

}
