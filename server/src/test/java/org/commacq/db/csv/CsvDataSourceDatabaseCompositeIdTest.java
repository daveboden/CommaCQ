package org.commacq.db.csv;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2;

import java.util.Collections;

import org.commacq.BlockCallback;
import org.commacq.CsvLine;
import org.commacq.db.ConfigDirectory;
import org.commacq.db.DataSourceAccess;
import org.commacq.db.EntityConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import com.google.common.collect.ImmutableList;

@RunWith(MockitoJUnitRunner.class)
public class CsvDataSourceDatabaseCompositeIdTest {
	
	@Mock
	private BlockCallback callback;
	
	private EmbeddedDatabase dataSource;
	
	@Before
	public void setupDataSource() {
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		dataSource = builder.setType(H2).addScript("classpath:/org/commacq/db/csv/compositeEntity-create-table-and-data.sql").build(); 
	}
	
	@After
	public void tearDownDataSource() {
		dataSource.shutdown();
	}
	
	@Test
	public void testBadGroupCausesError() throws Exception {
		DataSourceAccess dataSourceAccess = new DataSourceAccess(dataSource);
		
		EntityConfig entityConfig = ConfigDirectory.parseEntityConfigsFromResource("classpath:/org/commacq/db/csv/compositeEntity-config").get("compositeEntity");
		
		assertEquals(Collections.emptySet(), entityConfig.getGroups());
		assertEquals(ImmutableList.of("site", "year", "month"), entityConfig.getCompositeIdColumns());
		final CsvDataSourceDatabase csvDataSourceDatabase = new CsvDataSourceDatabase(dataSourceAccess, entityConfig);
		
		
		BlockCallback callback = mock(BlockCallback.class);
		
		csvDataSourceDatabase.getCsvLine("NY/2014/3", callback);
		
		ArgumentCaptor<CsvLine> captor = ArgumentCaptor.forClass(CsvLine.class);
		ArgumentCaptor<String> csvHeaderCaptor = ArgumentCaptor.forClass(String.class);
		verify(callback).processUpdate(anyString(), csvHeaderCaptor.capture(), captor.capture());
		assertEquals("id,site,year,month,revenue", csvHeaderCaptor.getValue());
		assertEquals("NY/2014/3,NY,2014,3,7000", captor.getValue().getCsvLine());
		verifyNoMoreInteractions(callback);
		
	}
}
