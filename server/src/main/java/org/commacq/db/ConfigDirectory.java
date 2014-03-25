package org.commacq.db;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Parses a directory full of .sql files which specify queries for
 * entity types.
 */
public final class ConfigDirectory {
	
	private static final String SQL_SUFFIX = ".sql";
	private static final String GROUPS_SUFFIX = ".groups.txt";
	private static final String ID_COLUMNS_SUFFIX = ".idColumns.txt";	
	
	
	private ConfigDirectory() {
		//No need to construct an object
	}
	
	/**
	 * Map of entity names to SQL
	 * Unmodifiable
	 * @return
	 */
	public static Map<String, EntityConfig> parseEntityConfigsFromResource(String resourceRoot) throws FileNotFoundException, IOException {
		ResourceLoader resourceLoader = new DefaultResourceLoader();
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(resourceLoader);
		Resource[] resources = resolver.getResources(resourceRoot + "/*" + SQL_SUFFIX);
		Map<String, EntityConfig> configs = new HashMap<String, EntityConfig>(resources.length);
		for(Resource resource : resources) {
			final String entityId = extractEntityId(resource.getFilename());
			final String sql = IOUtils.toString(resource.getInputStream());
			
			Resource groupsResource = resource.createRelative("./" + entityId + GROUPS_SUFFIX);
			
			final Set<String> groups;
			if(groupsResource.exists()) {
				List<String> groupLines = IOUtils.readLines(groupsResource.getInputStream());
				groups = new HashSet<String>(groupLines.size());
				for(String groupLine : groupLines) {
					if(StringUtils.isNotBlank(groupLine)) {
						groupLine = StringUtils.strip(groupLine);
						boolean notAlreadyPresent = groups.add(groupLine);
						if(!notAlreadyPresent) {
							throw new RuntimeException("Duplicate group defined - " + groupLine + " - in resource :" + groupsResource);
						}
					}
				}
			} else {
				groups = Collections.emptySet();
			}
			
			Resource idColumnsResource = resource.createRelative("./" + entityId + ID_COLUMNS_SUFFIX);
			final List<String> idColumns;
			if(idColumnsResource.exists()) {
				List<String> idColumnLines = IOUtils.readLines(idColumnsResource.getInputStream());
				idColumns = new ArrayList<String>();
				for(String line : idColumnLines) {
					if(StringUtils.isNotBlank(line)) {
						line = StringUtils.strip(line);
						boolean notAlreadyPresent = idColumns.add(line);
						if(!notAlreadyPresent) {
							throw new RuntimeException("Duplicate composite key column defined - " + line + " - in resource :" + idColumnsResource);
						}
					}
				}
			} else {
				idColumns = null;
			}
			
			configs.put(entityId, new EntityConfig(entityId, sql, groups, idColumns));
			
		}
		return Collections.unmodifiableMap(configs);
	}
	
	private static String extractEntityId(String filename) {
		return filename.substring(0, filename.length() - 4);
	}
}