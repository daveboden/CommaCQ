package org.commacq.client;

import java.util.Collection;
import java.util.Map;

import org.commacq.CsvLine;

public interface CsvToBeanConverter<BeanType> {

	Map<String, BeanType> getBeans(String columnNamesCsv, Collection<CsvLine> csvLines);
	BeanType getBean(String columnNamesCsv, CsvLine csvLine);
	
	Class<BeanType> getBeanType();
	
}
