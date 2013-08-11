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
import org.springframework.jdbc.core.RowMapper;

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
    
    private class CsvRowMapper implements RowMapper<CsvLine> {
    	
    	final CsvMarshaller csvParser = new CsvMarshaller();
    	
	    @Override
    	public CsvLine mapRow(ResultSet result, int rowNum) throws SQLException {
	    	return csvParser.toCsvLine(result);
    	}
    }
    
}