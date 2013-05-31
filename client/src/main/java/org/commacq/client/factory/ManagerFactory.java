package org.commacq.client.factory;

import org.commacq.client.BeanCacheUpdater;
import org.commacq.client.Manager;

public interface ManagerFactory {

    <BeanType> Manager<BeanType> createManager(BeanCacheUpdater<BeanType> beanCacheUpdater) throws ClassNotFoundException;
    <BeanType> Manager<BeanType> createManager(BeanCacheUpdater<BeanType> beanCacheUpdater, Class<? extends Manager<BeanType>> managerType) throws ClassNotFoundException;
	
}