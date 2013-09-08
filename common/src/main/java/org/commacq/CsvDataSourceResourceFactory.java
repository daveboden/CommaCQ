package org.commacq;

import java.io.IOException;

import org.apache.commons.lang3.text.WordUtils;
import org.springframework.core.io.Resource;

public class CsvDataSourceResourceFactory implements CsvDataSourceFactory {

	private final Resource resourceBase;
	private final boolean capitalizeEntityName;
	private final String extension;
	
	public CsvDataSourceResourceFactory(Resource resourceBase, boolean capitalizeEntityName, String extension) {
		this.resourceBase = resourceBase;
		this.capitalizeEntityName = capitalizeEntityName;
		this.extension = extension;
	}
	
	@Override
	public CsvDataSource createCsvDataSource(String entityId) {
		String entityLocation = capitalizeEntityName ? WordUtils.capitalize(entityId) : entityId;
		if(extension != null) {
			entityLocation += extension;
		}
		
		Resource resource;
		try {
			resource = resourceBase.createRelative(entityLocation);
		} catch (IOException ex) {
			throw new RuntimeException("Could not create resource for: " + entityId, ex);
		}
		
		return new CsvDataSourceResource(entityId, resource);
	}
	
}
