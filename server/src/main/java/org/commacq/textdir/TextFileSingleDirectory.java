package org.commacq.textdir;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
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
public class TextFileSingleDirectory<ROW> {
	
	private static final String SUFFIX = ".txt";
	
	private static final ResourceLoader resourceLoader = new DefaultResourceLoader();

    private final String directory;
    
    public TextFileSingleDirectory(String entityId, String directory) {
    	
        this.directory = directory;
        
        log.info("Successfully created text file single directory source with entity id {} at: {}",
                 entityId, directory);
    }
    
    public List<ROW> getAll(TextFileMapper<ROW> mapper) {
		ResourceLoader resourceLoader = new DefaultResourceLoader();
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(resourceLoader);
		String resourcesString = directory + "/*" + SUFFIX;
		Resource[] resources;
		try {
			resources = resolver.getResources(resourcesString);
		} catch (IOException ex) {
			throw new RuntimeException("Could not resolve items in directory: " + directory, ex);
		};
		
		List<ROW> rows = new ArrayList<ROW>();
		
		for(Resource resource : resources) {
			String id = extractEntityId(resource.getFilename());
			String text;
			try {
				text = IOUtils.toString(resource.getInputStream());
			} catch (IOException ex) {
				throw new RuntimeException("Could not read file: " + id);
			}
			
			rows.add(mapper.mapTextFile(id, text));
		}
		
		return rows;
    }
    
	private static String extractEntityId(String filename) {
		return filename.substring(0, filename.length() - SUFFIX.length());
	}
    
	public ROW getRow(String id, TextFileMapper<ROW> mapper) {
        //Get info from file id.txt
    	String location = directory + "/" + id + SUFFIX;
        Resource idResource = resourceLoader.getResource(location);
        String text;
		try {
			text = IOUtils.toString(idResource.getInputStream());
			return mapper.mapTextFile(id, text);
		} catch (IOException ex) {
			log.debug("Could not read file: {} - returning null", idResource);
			return null;
		}
		
	}
    
}
