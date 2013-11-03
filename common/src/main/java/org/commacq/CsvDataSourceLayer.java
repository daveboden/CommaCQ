package org.commacq;

import java.util.Map;
import java.util.SortedSet;

public interface CsvDataSourceLayer {

	SortedSet<String> getEntityIds();
	
	String getCsvEntry(String entityId, String id);

	String pokeCsvEntry(String entityId, String id);

	CsvDataSource getCsvDataSource(String entityId);

	Map<String, CsvDataSource> getMap();

}