package org.commacq.db;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.sql.DataSource;

import org.commacq.client.BeanCacheFactory;
import org.commacq.client.Manager;
import org.commacq.client.UpdateManager;
import org.commacq.client.factory.ManagerFactory;
import org.commacq.testclient.Holiday;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;

public class HolidayIT extends SharedServices {

	@Resource
	ApplicationContext sharedContext;
	
	GenericXmlApplicationContext testContext;
	
    @Resource
    DataSource integrationDataSource1;

    UpdateManager updateManager;

    Manager<Holiday> holidayManager;
    
    @SuppressWarnings("unchecked")
	@Before
    public void setup() throws Exception {
    	JdbcTemplate template = new JdbcTemplate(integrationDataSource1);
    	DefaultResourceLoader loader = new DefaultResourceLoader();
    	
    	JdbcTestUtils.executeSqlScript(template, loader, "classpath:integration-1/setup/holidayTable.sql", false);
    	JdbcTestUtils.executeSqlScript(template, loader, "classpath:integration-1/setup/holidayTableData.sql", false);
    	
    	testContext = new GenericXmlApplicationContext();
    	testContext.setParent(sharedContext);
    	Properties properties = new Properties();
    	properties.setProperty("database.sql.directory", "classpath:/integration-1/server-config/sql/holiday");
    	testContext.getEnvironment().getPropertySources().addLast(new PropertiesPropertySource("testProperties", properties));
    	testContext.load("classpath:/integration-1/PrimaryServer.xml");
    	testContext.load("classpath:/integration-1/PrimaryServer.Client.xml");
    	testContext.refresh();
    	testContext.start();
    	updateManager = testContext.getBean("updateManager", UpdateManager.class);
    	
    	BeanCacheFactory beanCacheFactory = testContext.getBean(BeanCacheFactory.class);
    	ManagerFactory managerFactory = testContext.getBean(ManagerFactory.class);
    	
    	holidayManager = (Manager<Holiday>)managerFactory.createManager(beanCacheFactory.createBeanCache("holiday"));
    }
    
    @After
    public void teardown() {
    	
    	JdbcTemplate template = new JdbcTemplate(integrationDataSource1);
    	template.execute("drop table HOLIDAY");
    }

    @Test
    public void testGroups() throws SQLException, IOException, JMSException, ClassNotFoundException {
    	
    }

}