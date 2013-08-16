package org.commacq.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.commacq.CsvCache;
import org.commacq.CsvDataSource;
import org.commacq.CsvMarshaller;
import org.commacq.CsvMarshaller.CsvLine;
import org.commacq.EntityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

/**
 * Makes a DataSourceAccess component into a CsvDataSource.
 */
public class CsvDataSourceDatabase implements CsvDataSource {

    private static Logger logger = LoggerFactory.getLogger(CsvDataSourceDatabase.class);

    private final CsvCacheFactory csvCacheFactory = new CsvCacheFactory();
    private final Map<String, EntityConfig> entityConfigs;
    private final SortedSet<String> entityIds;
    private final DataSourceAccess dataSourceAccess;
    
    public CsvDataSourceDatabase(DataSourceAccess dataSourceAccess, Map<String, EntityConfig> entityConfigs) {
    	this.dataSourceAccess = dataSourceAccess;
        this.entityConfigs = entityConfigs;
        
        SortedSet<String> names = new TreeSet<>();
        for(String entityName : entityConfigs.keySet()) {
            names.add(entityName);
        }
        
        entityIds = Collections.unmodifiableSortedSet(names);
        
        logger.info("Successfully created database CSV source with entity names: {}", entityConfigs.keySet());
    }
    
    public Map<String, CsvCache> createInitialCaches() {
        final Map<String, CsvCache> caches = new HashMap<>(entityConfigs.size());
        
        for(Map.Entry<String, EntityConfig> entry : entityConfigs.entrySet()) {

            CsvCache csvCache = createInitialCache(entry.getKey(), entry.getValue());
            
            caches.put(entry.getKey(), csvCache);
        }
        
        return caches;
    }
    
    public CsvCache createInitialCache(String entityId) {
        EntityConfig entityConfig = entityConfigs.get(entityId);
        
        CsvCache csvCache = createInitialCache(entityId, entityConfig);
        
        return csvCache;
    }
    
    private CsvCache createInitialCache(String entityId, EntityConfig entityConfig) {
    	return dataSourceAccess.getResultSetForAllRows(entityConfig, csvCacheFactory);
    }
    
    @Override
    public SortedSet<String> getEntityIds() {
        return entityIds;
    }
    
    @Override
    public CsvLine getCsvLine(String entityId, String id) {
        EntityConfig entityConfig = entityConfigs.get(entityId);
        
        return dataSourceAccess.getResultSetForSingleRow(entityConfig, new CsvRowMapper(), id);
    }

    public List<CsvLine> getCsvLines(final String entityId, final Collection<String> ids) {
        EntityConfig entityConfig = entityConfigs.get(entityId);
        
        List<CsvLine> result = dataSourceAccess.getResultSetForMultipleRows(entityConfig, new CsvRowMapper(), ids);
        
        return result;
    }
    
    public List<CsvLine> getCsvLinesForGroup(final String entityId, final String group, final String idWithinGroup) {
        EntityConfig entityConfig = entityConfigs.get(entityId);
        
        List<CsvLine> result = dataSourceAccess.getResultSetForGroup(entityConfig, new CsvRowMapper(), group, idWithinGroup);
        
        return result;
    }
    
    private class CsvRowMapper implements RowMapper<CsvLine> {
    	
    	final CsvMarshaller csvParser = new CsvMarshaller();
    	
	    @Override
    	public CsvLine mapRow(ResultSet result, int rowNum) throws SQLException {
	    	return csvParser.toCsvLine(result);
    	}
    }
    
    /**
     * Contains a linked list of CSV lines that can be traversed and written
     * to an output stream quickly in order.
     * 
     * The first column must be labelled "id".
     * 
     * Keeps track of the header fields and makes sure the fields get added
     * in the correct order on each line.
     */
    private final class CsvCacheFactory implements ResultSetExtractor<CsvCache> {

    	private final CsvMarshaller csvParser = new CsvMarshaller();
    	
    	@Override
    	public CsvCache extractData(ResultSet result) throws SQLException, DataAccessException {	
    		String columnNamesCsv = csvParser.getColumnLabelsAsCsvLine(result);
    		
    		CsvCache csvCache = new CsvCache(columnNamesCsv);
    		
    		while(result.next()) {
    			CsvLine csvLine = csvParser.toCsvLine(result);
    			csvCache.updateLine(csvLine);
    		}
    		
    		return csvCache;
    	}
    	
    }
    
}