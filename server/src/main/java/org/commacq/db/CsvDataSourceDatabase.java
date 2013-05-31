package org.commacq.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.commacq.CsvCache;
import org.commacq.CsvDataSource;
import org.commacq.CsvParser;
import org.commacq.CsvParser.CsvLine;
import org.commacq.EntityConfig;

public class CsvDataSourceDatabase implements CsvDataSource {

    private static Logger logger = LoggerFactory.getLogger(CsvDataSourceDatabase.class);

    private final CsvCacheFactory csvCacheFactory = new CsvCacheFactory();
    private final DataSource dataSource;
    private final Map<String, EntityConfig> entityConfigs;
    private final SortedSet<String> entityNames;
    
    public CsvDataSourceDatabase(DataSource dataSource, Map<String, EntityConfig> entityConfigs) {
        this.dataSource = dataSource;
        this.entityConfigs = entityConfigs;
        
        SortedSet<String> names = new TreeSet<>();
        for(String entityName : entityConfigs.keySet()) {
            names.add(entityName);
        }
        
        entityNames = Collections.unmodifiableSortedSet(names);
        
        logger.info("Successfully created database CSV source with entity names: {}", entityConfigs.keySet());
    }
    
    public Map<String, CsvCache> createInitialCaches() {
        Map<String, CsvCache> caches = new HashMap<>(entityConfigs.size());
        
        try(Connection con = dataSource.getConnection(); Statement stat = con.createStatement()) {
            for(Map.Entry<String, EntityConfig> entry : entityConfigs.entrySet()) {

                CsvCache csvCache = createInitialCache(entry.getKey(), entry.getValue(), stat);
                
                caches.put(entry.getKey(), csvCache);
            }
        } catch(SQLException ex) {
            throw new RuntimeException("Error executing SQL:\r\n" + ex);
        }
        return caches;
    }
    
    public CsvCache createInitialCache(String entityId) {
        try(Connection con = dataSource.getConnection(); Statement stat = con.createStatement()) {
            EntityConfig entityConfig = entityConfigs.get(entityId);
            
            CsvCache csvCache = createInitialCache(entityId, entityConfig, stat);
            
            return csvCache;
        } catch(SQLException ex) {
            throw new RuntimeException("Error executing SQL: " + ex);
        }
    }
    
    private CsvCache createInitialCache(String entityId, EntityConfig entityConfig, Statement stat) throws SQLException {
        try (
            ResultSet result = stat.executeQuery(entityConfig.getSql())
        ) {
            CsvCache csvCache = csvCacheFactory.createCsvCache(result);
            return csvCache;
        } catch(SQLException ex) {
            logger.error("Error creating entity: {} - error is: {}", entityId, ex.getMessage());
            throw ex;
        }
    }
    
    public SortedSet<String> getEntityNames() {
        return entityNames;
    }
    
    
    @Override
    public CsvLine getCsvLine(String entityId, String id) {
        EntityConfig entityConfig = entityConfigs.get(entityId);
        
        StringBuilder sql = new StringBuilder();
        //TODO probably best to cache this "nest" string arrangement
        sql.append("select nest.* from (").append(entityConfig.getSql()).append(") as nest ")
           .append("where nest.id = '").append(id).append("'");
        
        String sqlString = sql.toString();
        logger.info("Executing SQL: {}", sqlString);
        
        final CsvParser csvParser = new CsvParser();
        
        try (
            Connection con = dataSource.getConnection();
            Statement stat = con.createStatement();
            ResultSet result = stat.executeQuery(sqlString);
        ) {
            if(result.next()) {
                CsvLine csvLine = csvParser.toCsvLine(result);
                return csvLine;
            } else {
                return null;
            }
        } catch(SQLException ex) {
            String message = "Error executing SQL with = clause";
            logger.error(message, ex);
            throw new RuntimeException(message, ex);
        }
    }

    public List<CsvLine> getCsvLines(final String entityId, final Collection<String> ids) {
        EntityConfig entityConfig = entityConfigs.get(entityId);
        
        StringBuilder sql = new StringBuilder();
        //TODO probably best to cache this "nest" string arrangement
        sql.append("select nest.* from (").append(entityConfig.getSql()).append(") as nest ")
           .append("where nest.\"id\" in (");
        
        Iterator<String> idsIterator = ids.iterator();
        appendId(sql, idsIterator);
        
        while(idsIterator.hasNext()) {
            sql.append(",");
            appendId(sql, idsIterator);
        }
        
        sql.append(")");
                
        String sqlString = sql.toString();
        logger.info("Executing SQL: {}", sqlString);
        
        final CsvParser csvParser = new CsvParser();
        
        List<CsvLine> lines = new ArrayList<>();
        
        try (
            Connection con = dataSource.getConnection();
            Statement stat = con.createStatement();
            ResultSet result = stat.executeQuery(sqlString);
        ) {
            
            while(result.next()) {
                CsvLine csvLine = csvParser.toCsvLine(result);
                lines.add(csvLine);
            }
        } catch(SQLException ex) {
            throw new RuntimeException("Error executing SQL with 'in' clause", ex);
        }
        
        return lines;
    }
    
    private void appendId(StringBuilder builder, Iterator<String> idsIterator) {
        builder.append("'").append(idsIterator.next()).append("'");
    }
    
}
