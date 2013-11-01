package org.commacq.db;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.sql.SQLException;

import javax.jms.JMSException;

import lombok.extern.slf4j.Slf4j;

import org.commacq.client.Manager;
import org.commacq.testclient.Customer;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

/**
 * Test a primary server and a separate secondary server proxying all the data from the
 * first.
 */
@Slf4j
public class ProxyServerIT {

    @Test
    public void testCustomerDatabase() throws SQLException, IOException, JMSException, ClassNotFoundException {
    	GenericXmlApplicationContext sharedContext = new GenericXmlApplicationContext("classpath:/shared/SharedServices.xml");
    	
    	GenericXmlApplicationContext primaryServerContext = new GenericXmlApplicationContext();
    	primaryServerContext.setParent(sharedContext);
    	primaryServerContext.load("classpath:/integration-1/PrimaryServer.xml");
    	primaryServerContext.refresh();
    	primaryServerContext.start();
    	
    	GenericXmlApplicationContext secondaryServerContext = new GenericXmlApplicationContext();
    	secondaryServerContext.setParent(sharedContext);
    	secondaryServerContext.load("classpath:/integration-secondary/SecondaryServer.xml");
    	secondaryServerContext.refresh();
    	secondaryServerContext.start();
    	
    	GenericXmlApplicationContext clientContext= new GenericXmlApplicationContext();
    	clientContext.setParent(sharedContext);
    	clientContext.load("classpath:/integration-secondary/SecondaryServer.Client.xml");
    	clientContext.refresh();
    	clientContext.start();
    	
    	Manager<Customer> customerManager = clientContext.getBean("customerManager", Manager.class);
    	
    	assertNotNull(customerManager.get("BMW"));
    }

}