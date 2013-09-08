package org.commacq.client;

public interface BeanCacheFactory {

	BeanCache<?> createBeanCache(String entityId) throws Exception;
	<BeanType> BeanCache<BeanType> createBeanCache(String entityId, Class<BeanType> beanType) throws Exception;
	
}
