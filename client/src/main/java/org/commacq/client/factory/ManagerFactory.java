package org.commacq.client.factory;

import org.commacq.client.BeanCache;
import org.commacq.client.Manager;

public interface ManagerFactory {

    <BeanType> Manager<BeanType> createManager(BeanCache<BeanType> beanCacheUpdater) throws ClassNotFoundException;
    <BeanType> Manager<BeanType> createManager(BeanCache<BeanType> beanCacheUpdater, Class<? extends Manager<BeanType>> managerType) throws ClassNotFoundException;
	
}