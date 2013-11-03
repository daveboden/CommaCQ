package org.commacq.db;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.sql.SQLException;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.commacq.client.Manager;
import org.commacq.testclient.Customer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Test a primary server and a separate secondary server proxying all the data from the
 * first.
 */
public class ProxyServerIT extends SharedServices {

	@Resource
	DataSource integrationDataSource1;
	
	@Resource
	ApplicationContext sharedContext;
	
	@Before
    public void setup() throws IOException {
    	JdbcTemplate template = new JdbcTemplate(integrationDataSource1);
    	DefaultResourceLoader loader = new DefaultResourceLoader();
    	template.execute(IOUtils.toString(loader.getResource("classpath:integration-1/setup/customerTableData.sql").getInputStream()));
    }
    
    @After
    public void teardown() {
    	JdbcTemplate template = new JdbcTemplate(integrationDataSource1);
    	template.execute("truncate table customer");
    }
	
    @SuppressWarnings("resource")
	@Test
    public void testCustomerDatabase() throws SQLException, IOException, JMSException, ClassNotFoundException {    	
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
    	
    	@SuppressWarnings("unchecked")
		Manager<Customer> customerManager = clientContext.getBean("customerManager", Manager.class);
    	
    	assertNotNull(customerManager.get("BMW"));
    }

}