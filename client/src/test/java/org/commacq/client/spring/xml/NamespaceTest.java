package org.commacq.client.spring.xml;

import static org.junit.Assert.assertEquals;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.commacq.client.Manager;
import org.commacq.testclient.Customer;
import org.commacq.testclient.Customer2;
import org.commacq.testclient.CustomerManager2;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:org/commacq/client/spring/xml/manager-client-test.xml"
})
public class NamespaceTest {

	@Resource(name="customerManager")
	private Manager<Customer> customerManager;
	
	@Resource
	private Manager<Customer2> customerManager2;
	
	@Resource
	private CustomerManager2 customerManager3;
	
	@Test
	public void testCustomerManagerConstruction() {
		assertEquals(Customer.class, customerManager.get("BMW").getClass());
		assertEquals(Customer2.class, customerManager2.get("BMW").getClass());
		assertEquals(Customer2.class, customerManager3.mustGetCustomer("BMW").getClass());
	}
	
}
