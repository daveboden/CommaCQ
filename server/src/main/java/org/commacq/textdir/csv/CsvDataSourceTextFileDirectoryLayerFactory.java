package org.commacq.textdir.csv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.commacq.CsvDataSourceLayer;
import org.commacq.CsvDataSourceLayerCollection;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Creates a layer from a single root directory by looking at all the
 * subdirectories and creating an entity configuration for each one.
 */

@Slf4j
public class CsvDataSourceTextFileDirectoryLayerFactory {
	
	public CsvDataSourceLayer create(String rootDirectory) {
		
		ResourceLoader resourceLoader = new DefaultResourceLoader();
		
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(resourceLoader);
		Resource[] resources;
		try {
			resources = resolver.getResources(rootDirectory + "/*");
		} catch (IOException ex) {
			throw new RuntimeException("Could not resolve items in root directory: " + rootDirectory, ex);
		};
		
		
		List<CsvDataSourceTextFileSingleDirectory> sources = new ArrayList<CsvDataSourceTextFileSingleDirectory>();
		
		for(Resource resource : resources) {
			try {
				File file = resource.getFile();
				if(!file.isDirectory()) {
					log.info("Ignoring non-directory {}", file.getName());
					continue;
				}
			} catch(IOException ex) {
				//Not a file resource, so ignore the fact we can't turn it into a file to check if it's a directory
			}
			String entityId = resource.getFilename();

			CsvDataSourceTextFileSingleDirectory source = new CsvDataSourceTextFileSingleDirectory(entityId, rootDirectory + "/" + entityId);

			sources.add(source);
		}
		
		return new CsvDataSourceLayerCollection(sources);
	}
	
}