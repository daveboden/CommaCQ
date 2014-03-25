package org.commacq.db.csv;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.commacq.CsvDataSource;
import org.commacq.CsvLine;
import org.commacq.CsvUpdateBlockException;
import org.commacq.LineCallback;
import org.commacq.db.DataSourceAccess;
import org.commacq.db.EntityConfig;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 * Makes a DataSourceAccess component into a CsvDataSource.
 * 
 * Marshals results into CSV.
 * 
 * Processes updates so that if multiple downstream sources can subscribe
 * to this source and all get notified of changes. If the update is trusted,
 * it propagates the change directly downstream. If the update is not trusted,
 * or the update is marked for reconciliation, it looks the CSV up from the
 * database and propagates that. Additionally, for reconciliation, a string
 * comparison is done between the incoming update and the database CSV. If
 * there are differences, an error is logged.
 */
@Slf4j
public class CsvDataSourceDatabase implements CsvDataSource {

    private final EntityConfig entityConfig;
    private final DataSourceAccess dataSourceAccess;
    
    public CsvDataSourceDatabase(DataSourceAccess dataSourceAccess, EntityConfig entityConfig) {
    	this.dataSourceAccess = dataSourceAccess;
        this.entityConfig = entityConfig;
        
        log.info("Successfully created database CSV source with entity id: {}", entityConfig.getEntityId());
    }
    
    @Override
    public String getEntityId() {
    	return entityConfig.getEntityId();
    }
    
    @Override
    public void getAllCsvLines(LineCallback callback) {
    	dataSourceAccess.getResultSetForAllRows(entityConfig, new CsvListFactory(callback, null));
    }
    
    @Override
    public void getCsvLine(String id, LineCallback callback) {
        dataSourceAccess.getResultSetForSingleRow(entityConfig, new CsvListFactory(callback, Collections.singleton(id)), id);
    }

    @Override
    public void getCsvLines(final Collection<String> ids, LineCallback callback) {     
        dataSourceAccess.getResultSetForMultipleRows(entityConfig, new CsvListFactory(callback, ids), ids);
    }
    
    @Override
    public void getCsvLinesForGroup(final String group, final String idWithinGroup, LineCallback callback) {        
        dataSourceAccess.getResultSetForGroup(entityConfig, new CsvListFactory(callback, null), group, idWithinGroup);
    }
    
    @Override
    public String getColumnNamesCsv() {
    	String columnNamesCsv = dataSourceAccess.getColumnMetadata(entityConfig, new ResultSetExtractor<String>() {
    		@Override
    		public String extractData(ResultSet rs) throws SQLException, DataAccessException {
    			CsvMarshaller csvParser = new CsvMarshaller();
    			String columnLabelsFromMetadata = csvParser.getColumnLabelsAsCsvLine(rs.getMetaData(), entityConfig.getGroups());
    			if(entityConfig.getCompositeIdColumns() != null) {
    				return "id," + columnLabelsFromMetadata;
    			} else {
    				return columnLabelsFromMetadata;
    			}
    		}
		});
    	return columnNamesCsv;
    }
    
    /**
     * Convert a resultset row into a CsvLine, line by line.
     */
    @RequiredArgsConstructor
    private final class CsvListFactory implements ResultSetExtractor<Void> {

    	
    	private final LineCallback callback;
    	/**
    	 * Use null if working with a bulk update. No need to work out which ids
    	 * are missing from the dataset because all the ids not mentioned in the
    	 * database will be naturally removed anyway.
    	 */
    	private final Collection<String> ids;
    	
    	private final CsvMarshaller csvParser = new CsvMarshaller();
    	
    	@Override
    	public Void extractData(ResultSet result) throws SQLException, DataAccessException {	
    		try {
    			extractDataInternal(result);
    		} catch(SQLException ex) {
    			log.error("Cancelling update", ex);
    			throw ex;
    		} catch(DataAccessException ex) {
    			throw ex;
	    	} catch(CsvUpdateBlockException ex) {
	    		throw new SQLException(ex);
	    	}
    		
    		return null;
    	}
    	
    	public void extractDataInternal(ResultSet result) throws SQLException, DataAccessException, CsvUpdateBlockException {
    		String columnNamesCsv = csvParser.getColumnLabelsAsCsvLine(result.getMetaData(), entityConfig.getGroups());
    		if(entityConfig.getCompositeIdColumns() != null) {
    			if(columnNamesCsv.startsWith("id,")) {
    				throw new RuntimeException("An entity with a compositeId must not specify an id in the sql definition: " + entityConfig.getEntityId());
    			}
    			
    			columnNamesCsv = "id," + columnNamesCsv;
    		}
    		
    		List<String> copyOfIds = null;
    		if(ids != null) {
    			copyOfIds = new ArrayList<String>(ids);
    		}
    		
    		String entityId = entityConfig.getEntityId();
    		
    		while(result.next()) {    			
    			CsvLine csvLine = csvParser.toCsvLine(result, entityConfig);
					callback.processUpdate(entityId, columnNamesCsv, csvLine);
    			if(ids != null) {
    				copyOfIds.remove(csvLine.getId());
    			}
    		}
    		
    		if(ids != null) {
				for(String remainingId : copyOfIds) {
					callback.processRemove(entityId, columnNamesCsv, remainingId);
				}
    		}
    	}
    	
    }
    
}
