package org.commacq.textdir.csv;

import java.util.Collection;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringEscapeUtils;
import org.commacq.CsvDataSource;
import org.commacq.CsvLine;
import org.commacq.CsvUpdateBlockException;
import org.commacq.LineCallback;
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
	private static final String CSV_COLUMN_HEADINGS = "id," + TEXT_ATTRIBUTE;

    private final String entityId;
    private final TextFileSingleDirectory<CsvLine> textFileSingleDirectory;
    private final CsvTextFileMapper mapper = new CsvTextFileMapper();
    
    public CsvDataSourceTextFileSingleDirectory(String entityId, String directory) {
        this.entityId = entityId;      
        this.textFileSingleDirectory = new TextFileSingleDirectory<CsvLine>(entityId, directory);
    }
    
    @Override
	public void getAllCsvLines(LineCallback callback) {
		List<CsvLine> csvLines = textFileSingleDirectory.getAll(mapper);
		for(CsvLine csvLine : csvLines) {
			try {
				callback.processUpdate(entityId, CSV_COLUMN_HEADINGS, csvLine);
			} catch (CsvUpdateBlockException ex) {
				log.info("Caller cannot process the response. Not sending any further data. "
					   + "The caller will have logged the reason for the error.");
				break;
			}
		}
	}

	@Override
	public void getCsvLines(Collection<String> ids, LineCallback callback) {
		try {
			for(String id : ids) {
				getCsvLineInternal(id, callback);
			}
		} catch (CsvUpdateBlockException ex) {
			log.info("Caller cannot process the response. Not sending any further data. "
		    	   + "The caller will have logged the reason for the error.");
		}
	}

	@Override
	public void getCsvLine(String id, LineCallback callback) {
		try {
			getCsvLineInternal(id, callback);
		} catch (CsvUpdateBlockException ex) {
			log.info("Caller cannot process the response. Not sending any further data. "
				   + "The caller will have logged the reason for the error.");
		}
	}
	
	private void getCsvLineInternal(String id, LineCallback callback) throws CsvUpdateBlockException {
		CsvLine csvLine = textFileSingleDirectory.getRow(id, mapper);
		if(csvLine != null) {
			callback.processUpdate(entityId, CSV_COLUMN_HEADINGS, csvLine);
		} else {
			callback.processRemove(entityId, CSV_COLUMN_HEADINGS, id);
		}
	}

	@Override
	public void getCsvLinesForGroup(String group, String idWithinGroup, LineCallback callback) {
		throw new UnsupportedOperationException("Groups are not supported by text file data sources");
	}
    
    @Override
    public String getEntityId() {
        return entityId;
    }
    
    @Override
    public String getColumnNamesCsv() {
    	return CSV_COLUMN_HEADINGS;
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
