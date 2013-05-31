package org.commacq.client.spring.xml;

import static org.mockito.Mockito.mock;

import org.commacq.client.BeanCacheUpdater;
import org.commacq.client.factory.BeanCacheUpdaterFactory;

public class BeanCacheUpdaterFactoryStub implements BeanCacheUpdaterFactory {
	@Override
	public BeanCacheUpdater<?> createBeanCacheUpdater(String entityName) throws ClassNotFoundException {
		BeanCacheUpdater<?> beanCacheUpdater = mock(BeanCacheUpdater.class);
		return beanCacheUpdater;
	}
	
	@Override
	public <BeanType> BeanCacheUpdater<BeanType> createBeanCacheUpdater(String entityName, Class<BeanType> beanType) {
		@SuppressWarnings("unchecked")
        BeanCacheUpdater<BeanType> beanCacheUpdater = mock(BeanCacheUpdater.class);
		return beanCacheUpdater;
	}
}