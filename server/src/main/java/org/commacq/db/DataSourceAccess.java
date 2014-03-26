package org.commacq.db;

import java.util.Collection;
import java.util.Iterator;

import javax.sql.DataSource;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.commacq.CompositeIdEncoding;
import org.commacq.CompositeIdEncodingEscaped;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 * Wraps a SQL data source and provides methods to get at entity data based on
 * information held within EntityConfig objects.
 * 
 * Independent from data format; a ResultSetExtractor is passed in
 * to convert the results of the query into data.
 */
@Slf4j
public class DataSourceAccess {

    private final JdbcTemplate jdbcTemplate;
    
    @Getter
    @Setter
    private CompositeIdEncoding encoding = new CompositeIdEncodingEscaped();
    
    public DataSourceAccess(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);        
    }
    
    
       
    public <T> T getResultSetForAllRows(EntityConfig entityConfig, ResultSetExtractor<T> resultSetExtractor) throws DataAccessException {
    	try {
    		//Debug hint - put a breakpoint on your ResultSetExtractor's extractData method. You're about to disappear into Spring...
	    	T result = jdbcTemplate.query(entityConfig.getSql(), resultSetExtractor);
	    	return result;
        } catch(DataAccessException ex) {
            log.error("Error creating entity: {} - error is: {}", entityConfig.getEntityId(), ex.getMessage());
            throw ex;
        }
    }
    
    
    public <T> T getResultSetForSingleRow(EntityConfig entityConfig, ResultSetExtractor<T> resultSetExtractor, String id) throws DataAccessException {
        StringBuilder sql = new StringBuilder();
        
        sql.append("select nest.* from (").append(entityConfig.getSql()).append(") as nest ");
        
        if(entityConfig.getCompositeIdColumns() == null) {
	        //TODO probably best to cache this "nest" string arrangement
	        sql.append("where nest.\"id\" = '").append(id).append("'");
        } else {
        	String[] components = encoding.parseCompositeIdComponents(id);
        	int numberOfColumns = entityConfig.getCompositeIdColumns().size();
        	if(components.length != numberOfColumns) {
        		throw new RuntimeException("id can't be parsed into " + numberOfColumns + " components: " + id);
        	}
        	
        	sql.append("where ");
        	
        	for(int i = 0; i < numberOfColumns; i++) {
        		sql.append("nest.\"")
        		   .append(entityConfig.getCompositeIdColumns().get(i))
        		   .append("\" = '")
        		   .append(components[i])
        		   .append("'");
        		
        		if(i != numberOfColumns - 1) {
        			sql.append(" and ");
        		}
        	}
        }
        
        String sqlString = sql.toString();
        log.debug("Executing SQL: {}", sqlString);
        
        try {
        	//Debug hint - put a breakpoint on your ResultSetExtractor's extractData method. You're about to disappear into Spring...
        	return jdbcTemplate.query(sqlString, resultSetExtractor);
        } catch(IncorrectResultSizeDataAccessException ex) {
        	return null;
        } catch(DataAccessException ex) {
            String message = "Error executing SQL with = clause";
            log.error(message, ex);        	
            throw ex;
        }
    }
    
    public <METADATA> METADATA getColumnMetadata(EntityConfig entityConfig, ResultSetExtractor<METADATA> resultSetExtractor) throws DataAccessException {
        StringBuilder sql = new StringBuilder();
        sql.append("select nest.* from (").append(entityConfig.getSql()).append(") as nest ")
           .append("where 1 = 2"); //Won't return any rows
        
        String sqlString = sql.toString();
        log.debug("Executing SQL: {}", sqlString);
        
        try {
        	return jdbcTemplate.query(sqlString, resultSetExtractor);
        } catch(DataAccessException ex) {
            String message = "Error executing SQL to get metadata";
            log.error(message, ex);        	
            throw ex;
        }
    }    
    
    public <T> T getResultSetForGroup(EntityConfig entityConfig, ResultSetExtractor<T> resultSetExtractor, String group, String idWithinGroup) throws DataAccessException {
    	if(!entityConfig.getGroups().contains(group)) {
    		throw new RuntimeException("Group " + group + " has not been declared in a .groups.txt file alongside the .sql file");
    	}
    	
        StringBuilder sql = new StringBuilder();
        //TODO probably best to cache this "nest" string arrangement
        sql.append("select nest.* from (").append(entityConfig.getSql()).append(") as nest ")
           .append("where nest.\"" + group + "\" = '").append(idWithinGroup).append("'");
        
        String sqlString = sql.toString();
        log.debug("Executing SQL: {}", sqlString);
        
        try {
        	//Debug hint - put a breakpoint on your ResultSetExtractor's extractData method. You're about to disappear into Spring...
        	return jdbcTemplate.query(sqlString, resultSetExtractor);
        } catch(IncorrectResultSizeDataAccessException ex) {
        	return null;
        } catch(DataAccessException ex) {
            String message = "Error executing SQL with = clause";
            log.error(message, ex);        	
            throw ex;
        }
    }

    public <T> T getResultSetForMultipleRows(final EntityConfig entityConfig, final ResultSetExtractor<T> resultSetExtractor, final Collection<String> ids) throws DataAccessException {        
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
        log.debug("Executing SQL: {}", sqlString);
        
        try {
        	//Debug hint - put a breakpoint on your ResultSetExtractor's extractData method or use debug step filters.
        	//You're about to disappear into Spring...
        	return jdbcTemplate.query(sqlString, resultSetExtractor);
        } catch(DataAccessException ex) {
            String message = "Error executing SQL with 'in' clause";
            log.error(message, ex);
            throw ex;
        }
    }
    
    private void appendId(StringBuilder builder, Iterator<String> idsIterator) {
        builder.append("'").append(idsIterator.next()).append("'");
    }
    
}
