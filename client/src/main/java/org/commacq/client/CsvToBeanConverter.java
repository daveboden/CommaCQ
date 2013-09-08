package org.commacq.client;

import java.util.List;
import java.util.Map;

import org.commacq.CsvLine;

public interface CsvToBeanConverter<BeanType> {

	Map<String, BeanType> getBeans(List<String> header, Map<String, List<String>> body);
	BeanType getBean(String columnNamesCsv, CsvLine csvLine);
	
	Class<BeanType> getBeanType();
	
}
