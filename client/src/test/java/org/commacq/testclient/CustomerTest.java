package org.commacq.testclient;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import org.commacq.client.BeanCacheUpdater;
import org.commacq.client.BeanCacheUpdaterResourceInitialLoad;
import org.commacq.client.CsvToBeanStrategySpringConstructor;
import org.commacq.client.Manager;

@Ignore //until we can allow unspecified nullable types to be ignored.
public class CustomerTest {

	@Test
	public void testCustomerBean() throws Exception {
		
		BeanCacheUpdater<Customer> beanCacheUpdater = new BeanCacheUpdaterResourceInitialLoad<>("customer", new ClassPathResource("/customers.csv"), new CsvToBeanStrategySpringConstructor<Customer>(Customer.class));
		Manager<Customer> customerManager = new Manager<>(beanCacheUpdater);
		
		assertEquals("BMW", customerManager.mustGet("BMW").getId());
	}
	
}
