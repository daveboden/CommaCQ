package org.commacq.db;

import java.util.Map;

public interface RowFactory<RowObjectType> {

	void setId(String idValue);
	void addValue(String columnName, String value);
	void setGroupValues(Map<String, String> groupValues);
	
	RowObjectType getObject();
	
}
