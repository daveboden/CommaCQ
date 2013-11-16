package org.commacq.client;

import org.commacq.layer.SubscribeLayer;

/**
 * Simple implementation of a BeanCacheFactory which just delegates
 * to a CsvDataSourceFactory and then wires the bean cache up with the
 * csv data source.
 */
public class BeanCacheFactoryCsvDataSourceFactory implements BeanCacheFactory {

	final SubscribeLayer layer;
	final CsvToBeanConverterFactory csvToBeanConverterFactory;
	
	public BeanCacheFactoryCsvDataSourceFactory(SubscribeLayer layer, CsvToBeanConverterFactory csvToBeanConverterFactory) {
		this.layer = layer;
		this.csvToBeanConverterFactory = csvToBeanConverterFactory;
	}

	@Override
	public BeanCache<?> createBeanCache(String entityId) throws Exception {
		CsvToBeanConverter<?> csvToBeanConverter;
		try {
			csvToBeanConverter = csvToBeanConverterFactory.getCsvBeanConverter(entityId);
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException("Could not find bean class", ex);
		}
		BeanCache<?> beanCache = new BeanCache<>(layer, entityId, csvToBeanConverter);
		return beanCache;
	}

	@Override
	public <BeanType> BeanCache<BeanType> createBeanCache(String entityId, Class<BeanType> beanType) throws Exception {
		CsvToBeanConverter<BeanType> csvToBeanConverter = csvToBeanConverterFactory.getCsvBeanConverter(entityId, beanType);
		BeanCache<BeanType> beanCache = new BeanCache<>(layer, entityId, csvToBeanConverter);
		return beanCache;
	}

}
