package org.commacq.db;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.commacq.EntityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

public class DataSourceAccess {

    private static Logger logger = LoggerFactory.getLogger(DataSourceAccess.class);

    private final JdbcTemplate jdbcTemplate;
    
    public DataSourceAccess(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);        
    }
       
    public <T> T getResultSetForAllRows(EntityConfig entityConfig, ResultSetExtractor<T> resultSetExtractor) throws DataAccessException {
    	try {
	    	T result = jdbcTemplate.query(entityConfig.getSql(), resultSetExtractor);
	    	return result;
        } catch(DataAccessException ex) {
            logger.error("Error creating entity: {} - error is: {}", entityConfig.getEntityId(), ex.getMessage());
            throw ex;
        }
    }
    
    
    public <T> T getResultSetForSingleRow(EntityConfig entityConfig, RowMapper<T> rowMapper, String id) throws DataAccessException {        
        StringBuilder sql = new StringBuilder();
        //TODO probably best to cache this "nest" string arrangement
        sql.append("select nest.* from (").append(entityConfig.getSql()).append(") as nest ")
           .append("where nest.id = '").append(id).append("'");
        
        String sqlString = sql.toString();
        logger.debug("Executing SQL: {}", sqlString);
        
        try {
        	return jdbcTemplate.queryForObject(sqlString, rowMapper);
        } catch(IncorrectResultSizeDataAccessException ex) {
        	return null;
        } catch(DataAccessException ex) {
            String message = "Error executing SQL with = clause";
            logger.error(message, ex);        	
            throw ex;
        }
    }

    public <T> List<T> getResultSetForMultipleRows(final EntityConfig entityConfig, final RowMapper<T> rowMapper, final Collection<String> ids) throws DataAccessException {        
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
        
        try {
        	return jdbcTemplate.query(sqlString, rowMapper);
        } catch(DataAccessException ex) {
            String message = "Error executing SQL with 'in' clause";
            logger.error(message, ex);
            throw ex;
        }
    }
    
    private void appendId(StringBuilder builder, Iterator<String> idsIterator) {
        builder.append("'").append(idsIterator.next()).append("'");
    }
    
}
