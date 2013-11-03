package org.commacq.client;

import java.util.Collection;
import java.util.Map;

import org.commacq.CsvLine;

/**
 * Takes a CSV header and a number of CSV rows and returns beans
 * of the specified type.
 */
public interface CsvToBeanStrategy {
	
	<BeanType> BeanType getBean(Class<BeanType> beanType, String columnNamesCsv, CsvLine csvLine);
	<BeanType> Map<String, BeanType> getBeans(Class<BeanType> beanType, String columnNamesCsv, Collection<CsvLine> csvLines);
	
}