package org.commacq.textdir;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import lombok.extern.slf4j.Slf4j;

import org.commacq.CsvCache;
import org.commacq.CsvDataSource;
import org.commacq.CsvMarshaller.CsvLine;
import org.commacq.db.CsvCacheFactory;
import org.springframework.core.io.Resource;

/**
 * The TextFileSingleDirectory data source is a directory representing an entity, containing
 * text files.
 */
@Slf4j
public class CsvDataSourceTextFileSingleDirectory implements CsvDataSource {

    private final CsvCacheFactory csvCacheFactory = new CsvCacheFactory();
    private final String entityId;
    private final Resource directory;
    
    public CsvDataSourceTextFileSingleDirectory(String entityId, Resource directory) {
    	
        this.entityId = entityId;
        this.directory = directory;
        
        log.info("Successfully created text file single directory CSV source with entity id {} at: {}",
                 entityId, directory);
    }
    
    @Override
    public Map<String, CsvCache> createInitialCaches() {
        return Collections.singletonMap(entityId, createInitialCache());
    }
    
    public CsvCache createInitialCache(String entityId) {
        if(!entityId.equals(this.entityId)) {
            throw new RuntimeException("Entity id " + entityId + " is not supported by this CsvDataSource. Only " +
                                       this.entityId + " is supported.");
        }
        
        return createInitialCache();
    }
    
    private CsvCache createInitialCache() {
        //Get info from all *.txt files in the directory
        return null;
    }
    
    @Override
    public SortedSet<String> getEntityIds() {
        return new TreeSet<>(Collections.singleton(entityId));
    }
    
    @Override
    public CsvLine getCsvLine(String entityId, String id) {
        //Get info from file id.txt
        return null;
    }

    public List<CsvLine> getCsvLines(final String entityId, final Collection<String> ids) {
        //Get info from all the files id1.txt, id2.txt etc.
        return null;
    }
    
}