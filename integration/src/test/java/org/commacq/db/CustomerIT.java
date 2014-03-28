package org.commacq.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import org.commacq.client.BeanCacheFactory;
import org.commacq.client.CacheObserver;
import org.commacq.client.Manager;
import org.commacq.client.UpdateManager;
import org.commacq.client.factory.ManagerFactory;
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

@Slf4j
public class CustomerIT extends SharedServices {

	@Resource
	ApplicationContext sharedContext;
	
	GenericXmlApplicationContext testContext;
	
    @Resource
    DataSource integrationDataSource1;

    UpdateManager updateManager;

    Manager<Customer> customerManager;
    
    @SuppressWarnings("unchecked")
	@Before
    public void setup() throws Exception {
    	JdbcTemplate template = new JdbcTemplate(integrationDataSource1);

    	JdbcTestUtils.executeSqlScript(template, new ClassPathResource("integration-1/setup/customerTable.sql"), false);
    	JdbcTestUtils.executeSqlScript(template, new ClassPathResource("integration-1/setup/customerTableData.sql"), false);
    	
    	testContext = new GenericXmlApplicationContext();
    	testContext.setParent(sharedContext);
    	Properties properties = new Properties();
    	properties.setProperty("database.sql.directory", "classpath:/integration-1/server-config/sql/customer");
    	testContext.getEnvironment().getPropertySources().addLast(new PropertiesPropertySource("testProperties", properties));
    	
    	testContext.load("classpath:/integration-1/PrimaryServer.xml");
    	testContext.load("classpath:/integration-1/PrimaryServer.Client.xml");
    	testContext.refresh();
    	testContext.start();
    	updateManager = testContext.getBean("updateManager", UpdateManager.class);
    	
    	BeanCacheFactory beanCacheFactory = testContext.getBean(BeanCacheFactory.class);
    	ManagerFactory managerFactory = testContext.getBean(ManagerFactory.class);
    	
    	customerManager = (Manager<Customer>)managerFactory.createManager(beanCacheFactory.createBeanCache("customer"));
    }
    
    @After
    public void teardown() {
    	
    	JdbcTemplate template = new JdbcTemplate(integrationDataSource1);
    	template.execute("drop table customer");
    }

    @Test
    public void testCustomerDatabase() throws SQLException, IOException, JMSException, ClassNotFoundException {
        final String BMW = "BMW";
        final String BMW_DESCRIPTION_OLD = "BMW Motors";
        final String BMW_DESCRIPTION_NEW = "BMW Motors PLC";

        log.debug("Taking a snapshot which should return the same results " +
                "throughout its (short) lifetime regardless of any updates");
        //Manager<Customer> customerManagerSnapshot = customerManager.getSnapshot();

        Customer bmw = customerManager.mustGet(BMW);
        assertEquals(BMW_DESCRIPTION_OLD, bmw.getDescription());

        JdbcTemplate jdbcTemplate = new JdbcTemplate(integrationDataSource1);
        jdbcTemplate.execute("update customer set description = '" + BMW_DESCRIPTION_NEW + "' where CODE = '" + BMW + "'");

        SimpleCacheObserver<Customer> cacheObserver = new SimpleCacheObserver<>();

        customerManager.addCacheObserver(cacheObserver);

        updateManager.sendUpdate("customer", BMW);

        assertTrue("Cache Observer callback got called", cacheObserver.isUpdated());
        assertFalse("Cache Observer did not receive any deletes", cacheObserver.isDeleted());

        customerManager.removeCacheObserver(cacheObserver);

        /*
        assertEquals("Snapshot manager's value should not have changed",
                     BMW_DESCRIPTION_OLD, customerManagerSnapshot.mustGet(BMW).getDescription());
        */
        assertEquals("Update should have occurred",
                     BMW_DESCRIPTION_NEW, customerManager.mustGet(BMW).getDescription());

        jdbcTemplate.execute("delete from customer where CODE = '" + BMW + "'");

        cacheObserver = new SimpleCacheObserver<>();
        customerManager.addCacheObserver(cacheObserver);

        updateManager.sendUpdate("customer", BMW);

        assertTrue("Cache Observer callback got called", cacheObserver.isDeleted());
        assertFalse("Cache Observer did not receive any updates, only deletes", cacheObserver.isUpdated());
    }

    static class SimpleCacheObserver<T> implements CacheObserver<T> {
        private boolean updated = false;
        private boolean deleted = false;

        @Override
        public void beansUpdated(Map<String, T> beans) {
            updated = true;
        }

        public boolean isUpdated() {
            return updated;
        }

        @Override
        public void beansDeleted(Set<String> ids) {
            deleted = true;
        }

        public boolean isDeleted() {
            return deleted;
        }
    }

}