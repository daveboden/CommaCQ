package org.commacq.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.springframework.util.ResourceUtils;

import org.commacq.EntityConfig;

/**
 * Parses a directory full of .sql files which specify queries for
 * entity types.
 */
public final class ConfigDirectory {
	
	private ConfigDirectory() {
		//No need to construct an object
	}
	
	/**
	 * Map of entity names to SQL
	 * Unmodifiable
	 * @return
	 */
	public static Map<String, EntityConfig> parseEntityConfigs(String url) throws FileNotFoundException, IOException {
		File directory = ResourceUtils.getFile(url);
		File[] files = directory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".sql");
			}
		});
		
		Map<String, EntityConfig> entityMap = new HashMap<>();
		
		for(File file : files) {
			String entityName = file.getName().substring(0, file.getName().length() - 4);
			entityMap.put(entityName, new EntityConfig(entityName, IOUtils.toString(new FileInputStream(file))));
		}
		
		return Collections.unmodifiableMap(entityMap);
	}
	
}
