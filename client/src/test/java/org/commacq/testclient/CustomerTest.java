package org.commacq.testclient;

import static org.junit.Assert.assertEquals;

import org.commacq.CsvDataSourceResource;
import org.commacq.client.BeanCache;
import org.commacq.client.CsvToBeanConverter;
import org.commacq.client.CsvToBeanConverterImpl;
import org.commacq.client.CsvToBeanStrategySpringConstructor;
import org.commacq.client.Manager;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

@Ignore //until we can allow unspecified nullable types to be ignored.
public class CustomerTest {

	@Test
	public void testCustomerBean() throws Exception {
		
		CsvDataSourceResource resource = new CsvDataSourceResource("customer", new ClassPathResource("/customer.csv"));
		
		CsvToBeanConverter<Customer> converter = new CsvToBeanConverterImpl<>(Customer.class, new CsvToBeanStrategySpringConstructor());
		
		BeanCache<Customer> beanCache = new BeanCache<>(resource, converter);
		
		Manager<Customer> customerManager = new Manager<>(beanCache);
		
		assertEquals("BMW", customerManager.mustGet("BMW").getId());
	}
	
}
