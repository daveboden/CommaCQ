package org.commacq.client;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;


/**
 * Fetches the initial data load over HTTP.
 * Does not subscribe for updates.
 * 
 * The only way that this class will ever deliver events to
 * cache observers is if it's decided that this class should
 * support re-initialisation.
 */
public class BeanCacheUpdaterResourceInitialLoad<BeanType> extends BeanCacheUpdaterBase<BeanType> {
	
	private static final Logger logger = LoggerFactory.getLogger(BeanCacheUpdaterResourceInitialLoad.class);

	private final String entityId;
	private final CsvToBeanStrategy<BeanType> csvToBeanStrategy;
	private final Map<String, BeanType> beanCache;
	
	public BeanCacheUpdaterResourceInitialLoad(String entityId, Resource resource, final CsvToBeanStrategy<BeanType> csvToBeanStrategy) {
		
		this.entityId = entityId;
		this.csvToBeanStrategy = csvToBeanStrategy;
		
		logger.info("Fetching entity data from {}", resource);
		
		String initialLoadText;
		try {
			initialLoadText = IOUtils.toString(resource.getInputStream());
		} catch(IOException ex) {
			throw new RuntimeException(ex);
		}
		
		beanCache = csvToBeanStrategy.getBeans(initialLoadText).getUpdated(); //No concurrency required because this manager is never updated
	}
	
	@Override
	public Map<String, BeanType> getInitializedBeanCache() {
		return beanCache;
	}
	
	@Override
	public Class<BeanType> getBeanType() {
		return csvToBeanStrategy.getBeanType();
	}
	
	@Override
	public String getEntityId() {
		return entityId;
	}
	
}