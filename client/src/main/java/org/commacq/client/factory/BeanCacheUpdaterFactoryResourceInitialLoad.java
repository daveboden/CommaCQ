package org.commacq.client.factory;

import java.io.IOException;

import org.apache.commons.lang3.text.WordUtils;
import org.springframework.core.io.Resource;

import org.commacq.client.BeanCacheUpdater;
import org.commacq.client.BeanCacheUpdaterResourceInitialLoad;
import org.commacq.client.CsvToBeanStrategySpringConstructor;

/**
 * Creates BeanCacheUpdaters that perform an initial load from a base resource URL
 * that is modified to include the entity name and optionally a suffix (e.g. .csv)
 */
public class BeanCacheUpdaterFactoryResourceInitialLoad implements
		BeanCacheUpdaterFactory {

	private final BeanTypeSelectionStrategy beanTypeSelectionStrategy;
	private final Resource resourceBase;
	private final boolean capitalizeEntityName;
	private final String extension;
	
	public BeanCacheUpdaterFactoryResourceInitialLoad(BeanTypeSelectionStrategy beanTypeSelectionStrategy, Resource resourceBase, boolean capitalizeEntityName, String extension) {
		this.beanTypeSelectionStrategy = beanTypeSelectionStrategy;
		this.resourceBase = resourceBase;
		this.capitalizeEntityName = capitalizeEntityName;
		this.extension = extension;
	}
	
	@Override
	public <BeanType> BeanCacheUpdater<BeanType> createBeanCacheUpdater(
			String entityName, Class<BeanType> beanType) {
		String entityLocation = capitalizeEntityName ? WordUtils.capitalize(entityName) : entityName;
		if(extension != null) {
			entityLocation += extension;
		}
		
		Resource resource;
		try {
			resource = resourceBase.createRelative(entityLocation);
		} catch (IOException ex) {
			throw new RuntimeException("Could not create BeanCacheUpdater for: " + entityName, ex);
		}
		
		return new BeanCacheUpdaterResourceInitialLoad<BeanType>(entityName, resource, new CsvToBeanStrategySpringConstructor<BeanType>(beanType));
	}

	@Override
	public BeanCacheUpdater<?> createBeanCacheUpdater(String entityName) throws ClassNotFoundException {
		return createBeanCacheUpdater(entityName, beanTypeSelectionStrategy.chooseBeanType(entityName));
	}

}
