package org.commacq.client.factory;

import org.commacq.client.BeanCacheUpdater;

/**
 * Convenient way to create bean cache updaters in Spring
 */
public interface BeanCacheUpdaterFactory {

	/**
	 * Creates a BeanCacheUpdater that fetches information for the provided entity
	 * and creates Java Beans of the specified type.
	 */
	<BeanType> BeanCacheUpdater<BeanType> createBeanCacheUpdater(String entityName, Class<BeanType> beanType);
	
	/**
	 * Creates a BeanCacheUpdater without requiring that the user specifies the
	 * class name of the Java Beans that will be created. The implementer of this
	 * method should use a naming convention to decide on the class to use based
	 * on the entity name.
	 * 
	 * If there is no appropriate naming convention, the implementer of this method
	 * may choose to throw an UnsupportedOperationException which means that
	 * the user is forced to specify the bean types when configuring managers.
	 */
	BeanCacheUpdater<?> createBeanCacheUpdater(String entityName) throws ClassNotFoundException;
	
}
