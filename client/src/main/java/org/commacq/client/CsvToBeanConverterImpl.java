package org.commacq.client;

import java.util.List;
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
	public Map<String, BeanType> getBeans(List<String> header, Map<String, List<String>> body) {
		return csvToBeanStrategy.getBeans(beanType, header, body);
	}
	
	public Class<BeanType> getBeanType() {
		return beanType;
	}
	
}
