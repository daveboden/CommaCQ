package org.commacq.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.commacq.client.BeanCacheUpdater;
import org.commacq.client.BeanCacheUpdaterJmsBroadcast;
import org.commacq.client.CacheObserver;
import org.commacq.client.CsvToBeanStrategy;
import org.commacq.client.CsvToBeanStrategySpringConstructor;
import org.commacq.client.Manager;
import org.commacq.client.UpdateManager;
import org.commacq.testclient.Customer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
    "classpath:integration-1/setup/database.xml",
    "classpath:integration-1/setup/integration-placeholder.xml",
    "classpath:commacqserver/test/jms-connection-test.xml",
    "classpath:commacqserver/test/jms-listener-test.xml",
    
    "classpath:commacqserver/jms-handlers.xml",
    "classpath:commacqserver/data-manager.xml",
    "classpath:commacqserver/csv-data-source-database.xml",
    "classpath:commacqserver/http.xml",
    
    "classpath:org/commacq/client/update-manager.xml"
})
public class CustomerIT {
	
	private static Logger logger = LoggerFactory.getLogger(CustomerIT.class);

	@Resource
	DataSource dataSource;
	
	@Resource
	UpdateManager updateManager;
	
	@Resource
	ConnectionFactory connectionFactory;
	
	@Test
	public void testCustomerDatabase() throws SQLException, IOException, JMSException {
		final String BMW = "BMW";
		final String BMW_DESCRIPTION_OLD = "BMW Motors";
		final String BMW_DESCRIPTION_NEW = "BMW Motors PLC";

		CsvToBeanStrategy<Customer> csvToBeanStrategy = new CsvToBeanStrategySpringConstructor<Customer>(Customer.class);
		BeanCacheUpdater<Customer> beanCacheUpdater = new BeanCacheUpdaterJmsBroadcast<Customer>("customer", csvToBeanStrategy, connectionFactory, "query", "broadcast.customer", 20);
		Manager<Customer> customerManager = new Manager<Customer>(beanCacheUpdater);
		logger.debug("Taking a snapshot which should return the same results " +
				     "throughout its (short) lifetime regardless of any updates");
		Manager<Customer> customerManagerSnapshot = customerManager.getSnapshot();
		
		Customer bmw = customerManager.mustGet(BMW);
		assertEquals(BMW_DESCRIPTION_OLD, bmw.getDescription());
	
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.execute("update customer set description = '" + BMW_DESCRIPTION_NEW + "' where CODE = '" + BMW + "'");		
		
		SimpleCacheObserver<Customer> cacheObserver = new SimpleCacheObserver<>();

		customerManager.addCacheObserver(cacheObserver);
		
		updateManager.sendUpdate("customer", BMW);
		
		assertTrue("Cache Observer callback got called", cacheObserver.isUpdated());
		assertFalse("Cache Observer did not receive any deletes", cacheObserver.isDeleted());
		
		customerManager.removeCacheObserver(cacheObserver);
		
		assertEquals("Snapshot manager's value should not have changed", 
				BMW_DESCRIPTION_OLD, customerManagerSnapshot.mustGet(BMW).getDescription());
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