package org.commacq.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Properties;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.commacq.client.BeanCacheFactory;
import org.commacq.client.Manager;
import org.commacq.client.factory.ManagerFactory;
import org.commacq.testclient.CompositeEntity;
import org.commacq.testclient.Customer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;

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
    	JdbcTestUtils.executeSqlScript(template, new ClassPathResource("integration-1/setup/customerTable.sql"), false);
    	JdbcTestUtils.executeSqlScript(template, new ClassPathResource("integration-1/setup/customerTableData.sql"), false);
    	JdbcTestUtils.executeSqlScript(template, new ClassPathResource("integration-1/setup/compositeEntityTable.sql"), false);
    	JdbcTestUtils.executeSqlScript(template, new ClassPathResource("integration-1/setup/compositeEntityTableData.sql"), false);
    }
    
    @After
    public void teardown() {
    	JdbcTemplate template = new JdbcTemplate(integrationDataSource1);
    	template.execute("drop table customer");
    	template.execute("drop table RevenueBySiteAndMonth");
    }
	
    @SuppressWarnings("resource")
	@Test
    public void testCustomerAndCompositeEntityDatabase() throws Exception {    	
    	GenericXmlApplicationContext primaryServerContext = new GenericXmlApplicationContext();
    	primaryServerContext.setParent(sharedContext);
    	Properties properties = new Properties();
    	properties.setProperty("database.sql.directory", "classpath:/integration-1/server-config/sql/proxyServer");
    	primaryServerContext.getEnvironment().getPropertySources().addLast(new PropertiesPropertySource("testProperties", properties));
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
    	
    	BeanCacheFactory beanCacheFactory = clientContext.getBean(BeanCacheFactory.class);
    	ManagerFactory managerFactory = clientContext.getBean(ManagerFactory.class);
    	
    	@SuppressWarnings("unchecked")
		Manager<Customer> customerManager = (Manager<Customer>)managerFactory.createManager(beanCacheFactory.createBeanCache("customer"));
    	@SuppressWarnings("unchecked")
    	Manager<CompositeEntity> compositeEntityManager = (Manager<CompositeEntity>)managerFactory.createManager(beanCacheFactory.createBeanCache("compositeEntity"));
    	
    	assertNotNull(customerManager.get("BMW"));
    	CompositeEntity compositeEntity = compositeEntityManager.getByCompositeId("NY", "2014", "4");
    	assertNotNull(compositeEntity);
    	
    	assertEquals(8000, compositeEntity.getRevenue().intValue());
    }

}