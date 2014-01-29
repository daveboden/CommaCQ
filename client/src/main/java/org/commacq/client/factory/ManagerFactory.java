package org.commacq.client.factory;

import org.commacq.client.BeanCache;

public interface ManagerFactory {

    <BeanType> Object createManager(BeanCache<BeanType> beanCacheUpdater) throws ClassNotFoundException;
    <BeanType> Object createManager(BeanCache<BeanType> beanCacheUpdater, Class<?> managerType) throws ClassNotFoundException;
	
}