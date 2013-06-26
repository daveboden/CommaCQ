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
import org.commacq.EntityConfig;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.ResourceUtils;

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
			String entityId = extractEntityId(file.getName());
			entityMap.put(entityId, new EntityConfig(entityId, IOUtils.toString(new FileInputStream(file))));
		}
		
		return Collections.unmodifiableMap(entityMap);
	}
	
	
	public static Map<String, EntityConfig> parseEntityConfigsFromResource(String resourceRoot) throws FileNotFoundException, IOException {
		ResourceLoader resourceLoader = new DefaultResourceLoader();
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(resourceLoader);
		Resource[] resources = resolver.getResources(resourceRoot + "/*.sql");
		Map<String, EntityConfig> configs = new HashMap<>(resources.length);
		for(Resource resource : resources) {
			String entityId = extractEntityId(resource.getFilename());
			configs.put(entityId, new EntityConfig(entityId, IOUtils.toString(resource.getInputStream())));
		}
		return configs;
	}
	
	private static String extractEntityId(String filename) {
		return filename.substring(0, filename.length() - 4);
	}
}