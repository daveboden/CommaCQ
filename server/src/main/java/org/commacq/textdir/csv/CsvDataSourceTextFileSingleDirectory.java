package org.commacq.textdir.csv;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.commacq.CsvLine;
import org.commacq.CsvLineCallback;
import org.commacq.CsvUpdatableDataSourceBase;
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
public class CsvDataSourceTextFileSingleDirectory extends CsvUpdatableDataSourceBase {
	
	private static final String TEXT_ATTRIBUTE = "text";
	private static final String CSV_COLUMN_HEADINGS = "id," + TEXT_ATTRIBUTE;

    private final String entityId;
    private final TextFileSingleDirectory<CsvLine> textFileSingleDirectory;
    private final CsvTextFileMapper mapper = new CsvTextFileMapper();
    
    public CsvDataSourceTextFileSingleDirectory(String entityId, String directory) {
        this.entityId = entityId;      
        this.textFileSingleDirectory = new TextFileSingleDirectory<>(entityId, directory);
    }
    
    @Override
	public void getAllCsvLines(CsvLineCallback callback) {
		List<CsvLine> csvLines = textFileSingleDirectory.getAll(mapper);
		for(CsvLine csvLine : csvLines) {
			callback.processUpdate(CSV_COLUMN_HEADINGS, csvLine);
		}
	}

	@Override
	public void getCsvLines(Collection<String> ids, CsvLineCallback callback) {
		for(String id : ids) {
			getCsvLine(id, callback);
		}
	}

	@Override
	public void getCsvLine(String id, CsvLineCallback callback) {
		CsvLine csvLine = textFileSingleDirectory.getRow(id, mapper);
		if(csvLine != null) {
			callback.processUpdate(CSV_COLUMN_HEADINGS, csvLine);
		} else {
			callback.processRemove(id);
		}
	}

	@Override
	public void getCsvLinesForGroup(String group, String idWithinGroup, CsvLineCallback callback) {
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
