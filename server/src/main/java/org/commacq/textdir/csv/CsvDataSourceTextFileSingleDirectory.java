package org.commacq.textdir.csv;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringEscapeUtils;
import org.commacq.CsvCache;
import org.commacq.CsvDataSource;
import org.commacq.CsvMarshaller.CsvLine;
import org.commacq.textdir.TextFileMapper;
import org.commacq.textdir.TextFileSingleDirectory;

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
	
	private static final String TEXT_ATTRIBUTE = "text";

    private final String entityId;
    private final TextFileSingleDirectory<CsvLine> textFileSingleDirectory;
    private final CsvTextFileMapper mapper = new CsvTextFileMapper();
    
    public CsvDataSourceTextFileSingleDirectory(String entityId, String directory) {
    	
        this.entityId = entityId;
        
        this.textFileSingleDirectory = new TextFileSingleDirectory<>(entityId, directory);

    }
    
    @Override
    public Map<String, CsvCache> createInitialCaches() {
        return Collections.singletonMap(entityId, createInitialCache(entityId));
    }
    
    @Override
    public CsvCache createInitialCache(String entityId) {
    	CsvCache csvCache = new CsvCache("id," + TEXT_ATTRIBUTE);
    	
    	List<CsvLine> csvLines = textFileSingleDirectory.getAll(entityId, mapper);
    	for(CsvLine csvLine : csvLines) {
    		csvCache.updateLine(csvLine);    		
    	}
    	
    	return csvCache;
    }
    
    @Override
    public SortedSet<String> getEntityIds() {
        return new TreeSet<>(Collections.singleton(entityId));
    }
    
    @Override
    public CsvLine getCsvLine(String entityId, String id) {
    	return textFileSingleDirectory.getRow(entityId, id, mapper);
    }

    public List<CsvLine> getCsvLines(final String entityId, final Collection<String> ids) {
    	return textFileSingleDirectory.getRows(entityId, ids, mapper);
    }
    
    protected class CsvTextFileMapper implements TextFileMapper<CsvLine> {
    	
    	@Override
    	public CsvLine mapTextFile(String id, String text) {
    		String idEscaped = StringEscapeUtils.escapeCsv(id);
    		String textEscaped = StringEscapeUtils.escapeCsv(text);
    		
    		return new CsvLine(id, idEscaped + "," + textEscaped);
        }
    }
    
}
