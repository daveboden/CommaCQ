package org.commacq.client;

import org.commacq.CsvDataSource;
import org.commacq.CsvDataSourceFactory;

/**
 * Simple implementation of a BeanCacheFactory which just delegates
 * to a CsvDataSourceFactory and then wires the bean cache up with the
 * csv data source.
 */
public class BeanCacheFactoryCsvDataSourceFactory implements BeanCacheFactory {

	final CsvDataSourceFactory csvDataSourceFactory;
	final CsvToBeanConverterFactory csvToBeanConverterFactory;
	
	public BeanCacheFactoryCsvDataSourceFactory(CsvDataSourceFactory csvDataSourceFactory, CsvToBeanConverterFactory csvToBeanConverterFactory) {
		this.csvDataSourceFactory = csvDataSourceFactory;
		this.csvToBeanConverterFactory = csvToBeanConverterFactory;
	}

	@Override
	public BeanCache<?> createBeanCache(String entityId) throws Exception {
		CsvDataSource csvDataSource = csvDataSourceFactory.createCsvDataSource(entityId);
		CsvToBeanConverter<?> csvToBeanConverter;
		try {
			csvToBeanConverter = csvToBeanConverterFactory.getCsvBeanConverter(entityId);
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException("Could not find bean class", ex);
		}
		BeanCache<?> beanCache = new BeanCache<>(csvDataSource, csvToBeanConverter);
		return beanCache;
	}

	@Override
	public <BeanType> BeanCache<BeanType> createBeanCache(String entityId, Class<BeanType> beanType) throws Exception {
		CsvDataSource csvDataSource = csvDataSourceFactory.createCsvDataSource(entityId);
		CsvToBeanConverter<BeanType> csvToBeanConverter = csvToBeanConverterFactory.getCsvBeanConverter(entityId, beanType);
		BeanCache<BeanType> beanCache = new BeanCache<>(csvDataSource, csvToBeanConverter);
		return beanCache;
	}

}
