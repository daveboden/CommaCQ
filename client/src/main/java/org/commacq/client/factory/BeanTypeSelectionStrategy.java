package org.commacq.client.factory;

public interface BeanTypeSelectionStrategy {

	Class<?> chooseBeanType(String entityId) throws ClassNotFoundException;
	
}
