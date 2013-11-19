package org.commacq.db;

import java.io.IOException;
import java.sql.SQLException;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.commacq.client.Manager;
import org.commacq.client.UpdateManager;
import org.commacq.testclient.Holiday;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;

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
    public void setup() throws IOException {
    	JdbcTemplate template = new JdbcTemplate(integrationDataSource1);
    	DefaultResourceLoader loader = new DefaultResourceLoader();
    	template.execute(IOUtils.toString(loader.getResource("classpath:integration-1/setup/holidayTableData.sql").getInputStream()));
    	
    	testContext = new GenericXmlApplicationContext();
    	testContext.setParent(sharedContext);
    	testContext.load("classpath:/org/commacq/db/HolidayIT-context.xml");
    	testContext.refresh();
    	testContext.start();
    	updateManager = testContext.getBean("updateManager", UpdateManager.class);
    	
    	holidayManager = testContext.getBean("holidayManager", Manager.class);
    }
    
    @After
    public void teardown() {
    	
    	JdbcTemplate template = new JdbcTemplate(integrationDataSource1);
    	template.execute("truncate table HOLIDAY");
    }

    @Test
    public void testGroups() throws SQLException, IOException, JMSException, ClassNotFoundException {
    	
    }

}