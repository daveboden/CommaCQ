package org.commacq.client;

import org.commacq.client.factory.BeanTypeSelectionStrategy;

public class CsvToBeanConverterFactory {

	final CsvToBeanStrategy csvToBeanStrategy;
	final BeanTypeSelectionStrategy beanTypeSelectionStrategy;
	
	public CsvToBeanConverterFactory(CsvToBeanStrategy csvToBeanStrategy, BeanTypeSelectionStrategy beanTypeSelectionStrategy) {
		this.csvToBeanStrategy = csvToBeanStrategy;
		this.beanTypeSelectionStrategy = beanTypeSelectionStrategy;
	}
	
	public CsvToBeanConverter<?> getCsvBeanConverter(String entityId) throws ClassNotFoundException {
		Class<?> beanType = beanTypeSelectionStrategy.chooseBeanType(entityId);
		return new CsvToBeanConverterImpl<>(beanType, csvToBeanStrategy);
	}
	
	public <BeanType> CsvToBeanConverter<BeanType> getCsvBeanConverter(String entityId, Class<BeanType> beanType) {
		return new CsvToBeanConverterImpl<>(beanType, csvToBeanStrategy);
	}
	
}
