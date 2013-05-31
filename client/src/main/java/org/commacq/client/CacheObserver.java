package org.commacq.client;

import java.util.Map;
import java.util.Set;

public interface CacheObserver<BeanType> {

	void beansUpdated(Map<String, BeanType> beans);
	void beansDeleted(Set<String> ids);
	
}
