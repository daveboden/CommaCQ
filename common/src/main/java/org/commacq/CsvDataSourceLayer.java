package org.commacq;

import java.util.Map;
import java.util.SortedSet;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;

public interface CsvDataSourceLayer {

	SortedSet<String> getEntityIds();
	
	String getCsvEntry(String entityId, String id);

	String pokeCsvEntry(String entityId, String id);

	CsvDataSource getCsvDataSource(String entityId);

	Map<String, CsvDataSource> getMap();

}