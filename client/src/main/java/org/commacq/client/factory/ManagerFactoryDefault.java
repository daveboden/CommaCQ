package org.commacq.client.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.commacq.client.BeanCache;
import org.commacq.client.Manager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The most basic strategy, which if given no extra info returns a basic
 * manager with no additional methods.
 * If passed a manager type, it assumes that the manager has a constructor
 * with a single argument of type BeanCache If you want to do something
 * more complicated when constructing your factory, implement a ManagerFactory.
 */
public class ManagerFactoryDefault implements ManagerFactory {
	
	Logger logger = LoggerFactory.getLogger(ManagerFactoryDefault.class);
	
    @Override
	public <BeanType> Manager<BeanType> createManager(BeanCache<BeanType> beanCache) {
		return new Manager<BeanType>(beanCache);
	}
    
    @Override
    public <BeanType> Manager<BeanType> createManager(BeanCache<BeanType> beanCache, Class<?> managerType) throws ClassNotFoundException {
    	try {
			@SuppressWarnings("unchecked")
			Constructor<? extends Manager<BeanType>> constructor = (Constructor<? extends Manager<BeanType>>)managerType.getConstructor(BeanCache.class);
			return constructor.newInstance(beanCache);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new ClassNotFoundException("Could not construct " + managerType.getName(), ex);
		}
    }
	
}