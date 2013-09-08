package org.commacq;

/**
 * Convenient way to create Csv Data Sources in Spring
 */
public interface CsvDataSourceFactory {

	/**
	 * Creates a CsvDataSource that fetches information for the provided entity.
	 */
	CsvDataSource createCsvDataSource(String entityId) throws Exception;

}