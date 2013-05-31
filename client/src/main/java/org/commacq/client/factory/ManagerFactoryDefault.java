package org.commacq.client.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.commacq.client.BeanCacheUpdater;
import org.commacq.client.Manager;

/**
 * The most basic strategy, which if given no extra info returns a basic
 * manager with no additional methods.
 * If passed a manager type, it assumes that the manager has a constructor
 * with a single argument of type BeanCacheUpdater. If you want to do something
 * more complicated when constructing your factory, implement a ManagerFactory.
 */
public class ManagerFactoryDefault implements ManagerFactory {
	
	Logger logger = LoggerFactory.getLogger(ManagerFactoryDefault.class);
	
    @Override
	public <BeanType> Manager<BeanType> createManager(BeanCacheUpdater<BeanType> beanCacheUpdater) {
		return new Manager<BeanType>(beanCacheUpdater);
	}
    
    @Override
    public <BeanType> Manager<BeanType> createManager(BeanCacheUpdater<BeanType> beanCacheUpdater, Class<? extends Manager<BeanType>> managerType) throws ClassNotFoundException {
    	try {
			Constructor<? extends Manager<BeanType>> constructor = managerType.getConstructor(BeanCacheUpdater.class);
			return constructor.newInstance(beanCacheUpdater);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new ClassNotFoundException("Could not construct " + managerType.getName(), ex);
		}
    }
	
}