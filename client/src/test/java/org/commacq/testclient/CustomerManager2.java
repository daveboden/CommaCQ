package org.commacq.testclient;

import org.commacq.client.BeanCache;
import org.commacq.client.Manager;

public class CustomerManager2 extends Manager<Customer2> {

	public CustomerManager2(BeanCache<Customer2> beanCacheUpdater) {
		super(beanCacheUpdater);
	}

	public Customer2 mustGetCustomer(String customer) {
		return mustGet(customer);
	}
	
}
