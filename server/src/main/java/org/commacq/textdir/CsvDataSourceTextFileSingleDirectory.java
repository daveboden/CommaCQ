package org.commacq.textdir;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.commacq.CsvCache;
import org.commacq.CsvDataSource;
import org.commacq.CsvMarshaller.CsvLine;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * The TextFileSingleDirectory data source is a directory representing an entity, containing
 * text files. The files get mounted as an entity with ids of the text filenames and "text"
 * attribute of the file context.
 * 
 * A directory containing files id1.txt and id2.txt will result in:
 * 
 * id,text
 * id1,Whatever the contents of id1.txt are
 * id2,Whatever the contents of id2.txt are
 */
@Slf4j
public class CsvDataSourceTextFileSingleDirectory implements CsvDataSource {
	
	private static final String SUFFIX = ".txt";
	private static final String TEXT_ATTRIBUTE = "text";
	
	private static final ResourceLoader resourceLoader = new DefaultResourceLoader();

    private final String entityId;
    private final String directory;
    
    public CsvDataSourceTextFileSingleDirectory(String entityId, String directory) {
    	
        this.entityId = entityId;
        this.directory = directory;
        
        log.info("Successfully created text file single directory CSV source with entity id {} at: {}",
                 entityId, directory);
    }
    
    @Override
    public Map<String, CsvCache> createInitialCaches() {
        return Collections.singletonMap(entityId, createInitialCache());
    }
    
    private void checkEntityId(String entityId) {
    	if(!entityId.equals(this.entityId)) {
    		throw new RuntimeException("Entity id " + entityId + " is not supported by this CsvDataSource. Only " +
    				this.entityId + " is supported.");
    	}    	
    }
    
    public CsvCache createInitialCache(String entityId) {
        checkEntityId(entityId);
        return createInitialCache();
    }
    
    private CsvCache createInitialCache() {
		ResourceLoader resourceLoader = new DefaultResourceLoader();
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(resourceLoader);
		Resource[] resources;
		try {
			resources = resolver.getResources(directory + "/*" + SUFFIX);
		} catch (IOException ex) {
			throw new RuntimeException("Could not resolve items in directory: " + directory, ex);
		};
		CsvCache csvCache = new CsvCache("id," + TEXT_ATTRIBUTE);
		for(Resource resource : resources) {
			String id = extractEntityId(resource.getFilename());
			String text;
			try {
				text = IOUtils.toString(resource.getInputStream());
			} catch (IOException ex) {
				throw new RuntimeException("Could not read file: " + id);
			}
			
			csvCache.updateLine(createCsvLine(id, text));
		}
		
		return csvCache;
    }
    
	private static String extractEntityId(String filename) {
		return filename.substring(0, filename.length() - SUFFIX.length());
	}
    
    @Override
    public SortedSet<String> getEntityIds() {
        return new TreeSet<>(Collections.singleton(entityId));
    }
    
    @Override
    public CsvLine getCsvLine(String entityId, String id) {
    	checkEntityId(entityId);
        //Get info from file id.txt
    	String location = directory + "/" + id + SUFFIX;
        Resource idResource = resourceLoader.getResource(location);
        if(idResource == null) {
			throw new RuntimeException("Could not obtain file: " + location);
		}
        String text;
		try {
			text = IOUtils.toString(idResource.getInputStream());
		} catch (IOException ex) {
			throw new RuntimeException("Could not read file: " + idResource);
		}
        
        return getCsvLine(id, text);
    }
    
    private CsvLine createCsvLine(String id, String text) {
		String idEscaped = StringEscapeUtils.escapeCsv(id);
		String textEscaped = StringEscapeUtils.escapeCsv(text);
		
		return new CsvLine(id, idEscaped + "," + textEscaped);
    }

    public List<CsvLine> getCsvLines(final String entityId, final Collection<String> ids) {
    	checkEntityId(entityId);
        //Get info from all the files id1.txt, id2.txt etc.
        List<CsvLine> lines = new ArrayList<>(ids.size());
        for(String id : ids) {
        	lines.add(getCsvLine(entityId, id));
        }
        return lines;
    }
    
}
