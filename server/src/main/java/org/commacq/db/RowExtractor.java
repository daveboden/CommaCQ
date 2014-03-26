package org.commacq.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;

import org.commacq.CompositeIdEncoding;

@RequiredArgsConstructor
public class RowExtractor {
	
	private final CompositeIdEncoding compositeIdEncoding;
	
	public static final StringColumnValueConverter stringColumnValueConverter = new StringColumnValueConverter();

	public <RowObjectType> RowObjectType extractRow(EntityConfig entityConfig, ResultSet result, RowFactory<RowObjectType> rowFactory) throws SQLException {
        ResultSetMetaData metaData = result.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        if(columnCount <= 0) {
        	throw new SQLException("No columns to consider");
        }
        
        int startFromColumn;
        if(entityConfig.getCompositeIdColumns() != null) {
        	//Haven't calculated the id yet; will have to prefix it to the line afterwards
        	startFromColumn = 1;
        } else {
        	String idValue = stringColumnValueConverter.getColumnValue(result, metaData.getColumnType(1), 1);
        	rowFactory.setId(idValue);
        	
        	startFromColumn = 2; //We've already processed column 1: id.
        }
        
        Map<String, String> groupValues = new HashMap<String, String>(entityConfig.getGroups().size());
        
        Map<String, String> compositeKeyValues = null;
        if(entityConfig.getCompositeIdColumns() != null) {
        	compositeKeyValues = new HashMap<String, String>(entityConfig.getCompositeIdColumns().size());
        }
        
        for (int i = startFromColumn; i <= metaData.getColumnCount(); i++) {
        	String columnLabel = metaData.getColumnLabel(i);
			String columnValue = stringColumnValueConverter.getColumnValue(result, metaData.getColumnType(i), i);
			rowFactory.addValue(columnLabel, columnValue);
			
			if(entityConfig.getGroups().contains(columnLabel)) {
				groupValues.put(columnLabel, columnValue);
			}
			if(compositeKeyValues != null) {
				if(entityConfig.getCompositeIdColumns().contains(columnLabel)) {
					compositeKeyValues.put(columnLabel, columnValue);
				}
			}
        }
        
        rowFactory.setGroupValues(groupValues);
        
        if(compositeKeyValues != null) {
        	String[] components = new String[entityConfig.getCompositeIdColumns().size()];
        	int index = 0;
        	for(String compositeColumn : entityConfig.getCompositeIdColumns()) {
        		String value = compositeKeyValues.get(compositeColumn);
        		if(value == null) {
        			throw new RuntimeException("Null value in composite key column: " + compositeColumn);
        		}
        		components[index++] = value;
        	}
        	String id = compositeIdEncoding.createCompositeId(components);
        	
        	rowFactory.setId(id);
        }
		
		return rowFactory.getObject();
	}
	
}
