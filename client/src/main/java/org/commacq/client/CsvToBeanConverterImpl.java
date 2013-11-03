package org.commacq.client;

import java.util.Collection;
import java.util.Map;

import org.commacq.CsvLine;

public class CsvToBeanConverterImpl<BeanType> implements CsvToBeanConverter<BeanType> {
	
	final Class<BeanType> beanType;
	final CsvToBeanStrategy csvToBeanStrategy;
	
	public CsvToBeanConverterImpl(Class<BeanType> beanType, CsvToBeanStrategy csvToBeanStrategy) {
		this.beanType = beanType;
		this.csvToBeanStrategy = csvToBeanStrategy;
	}
	
	@Override
	public BeanType getBean(String columnNamesCsv, CsvLine csvLine) {
		return csvToBeanStrategy.getBean(beanType, columnNamesCsv, csvLine);
	}
	
	@Override
	public Map<String, BeanType> getBeans(String columnNamesCsv, Collection<CsvLine> csvLines) {
		return csvToBeanStrategy.getBeans(beanType, columnNamesCsv, csvLines);
	}
	
	public Class<BeanType> getBeanType() {
		return beanType;
	}
	
}
