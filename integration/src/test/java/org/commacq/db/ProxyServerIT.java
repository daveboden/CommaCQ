package org.commacq.db;

import java.io.IOException;
import java.sql.SQLException;

import javax.jms.JMSException;

import lombok.extern.slf4j.Slf4j;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test a primary server and a separate secondary server proxying all the data from the
 * first.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Slf4j
@Ignore
public class ProxyServerIT {

    @Test
    public void testCustomerDatabase() throws SQLException, IOException, JMSException, ClassNotFoundException {
    	GenericXmlApplicationContext sharedContext = new GenericXmlApplicationContext("classpath:/org/commacq/db/SharedServices.xml");
    	
    	GenericXmlApplicationContext primaryServerContext = new GenericXmlApplicationContext("classpath:/org/commacq/db/PrimaryServer.xml");
    	primaryServerContext.setParent(sharedContext);
    	
    	GenericXmlApplicationContext secondaryServerContext = new GenericXmlApplicationContext("classpath:/org/commacq/db/SecondaryServer.xml");
    	secondaryServerContext.setParent(sharedContext);
    	
    	GenericXmlApplicationContext clientContext= new GenericXmlApplicationContext("classpath:/org/commacq/db/SecondaryServer.Client.xml");
    	
    	
    	
    }

}