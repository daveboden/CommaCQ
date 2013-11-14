package org.commacq;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;

/**
 * Scans an entire CSV file every time all lines are requested.
 * Maintains a list of observers. Re-scans the file and calls the
 * observers back only if a specific reload() method is called.
 */
public class CsvDataSourceResource implements CsvDataSource {

	private final String entityId;
	private final Resource resource;
	private final CsvTextBlockToCallback csvTextBlockToCallback = new CsvTextBlockToCallback();
	
	public CsvDataSourceResource(String entityId, Resource resource) {
		this.entityId = entityId;
		this.resource = resource;
	}
	
	@Override
	public String getEntityId() {
		return entityId;
	}
	
	@Override
	public void getAllCsvLines(CsvLineCallback callback) {
		presentAllLinesToCallback(callback);
	}
	
	@Override
	public String getColumnNamesCsv() {
		try {
			String header = csvTextBlockToCallback.getCsvColumnNames(new InputStreamReader(resource.getInputStream()));
			return header;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	private void presentAllLinesToCallback(CsvLineCallback callback) {
		String initialLoadText;
		try {
			initialLoadText = IOUtils.toString(resource.getInputStream());
		} catch(IOException ex) {
			throw new RuntimeException(ex);
		}
		
		csvTextBlockToCallback.presentTextBlockToCsvLineCallback(entityId, initialLoadText, callback, true);
	}
	
	@Override
	public void getCsvLine(String id, CsvLineCallback callback) {
		throw new UnsupportedOperationException("Only all lines currently supported");
	}
	
	@Override
	public void getCsvLines(Collection<String> ids, CsvLineCallback callback) {
		throw new UnsupportedOperationException("Only all lines currently supported");
	}
	
	@Override
	public void getCsvLinesForGroup(String group, String idWithinGroup, CsvLineCallback callback) {
		throw new UnsupportedOperationException("Only all lines currently supported");
	}
	
}
