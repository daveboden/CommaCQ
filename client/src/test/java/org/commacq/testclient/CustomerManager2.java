package org.commacq.testclient;

import org.commacq.client.BeanCacheUpdater;
import org.commacq.client.Manager;

public class CustomerManager2 extends Manager<Customer2> {

	public CustomerManager2(BeanCacheUpdater<Customer2> beanCacheUpdater) {
		super(beanCacheUpdater);
	}

	public Customer2 mustGetCustomer(String customer) {
		return mustGet(customer);
	}
	
}
