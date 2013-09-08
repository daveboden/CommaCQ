package org.commacq.db.csv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.RequiredArgsConstructor;

import org.commacq.CsvDataSource;
import org.commacq.CsvDataSourceLayer;
import org.commacq.db.DataSourceAccess;
import org.commacq.db.EntityConfig;

/**
 * Takes a ConfigDirectory which holds a number of EntityConfig objects and
 * iterates through them to produce a CsvDataSourceDatabase for each entity.
 */
@RequiredArgsConstructor
public class CsvDataSourceDatabaseFactory {
	
	private final DataSourceAccess dataSourceAccess;

	public CsvDataSourceLayer create(Map<String, EntityConfig> entityConfigs) {
		
		List<CsvDataSource> sources = new ArrayList<CsvDataSource>();
		for(Entry<String, EntityConfig> entry : entityConfigs.entrySet()) {
			CsvDataSourceDatabase source = new CsvDataSourceDatabase(dataSourceAccess, entry.getValue());
			sources.add(source);
		}
		return new CsvDataSourceLayer(sources);
	}
	
}
