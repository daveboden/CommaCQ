package org.commacq.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

public class ConfigDirectoryTest {

	@Test
	public void testResourceScanning() throws IOException {
		Map<String, EntityConfig> config = ConfigDirectory.parseEntityConfigsFromResource("classpath:/org/commacq/db/test-sql");
		
		assertEquals(1, config.size());
		assertTrue(config.containsKey("example"));
	}
	
	@Test
	public void testGroupsHolidayExample() throws IOException {
		Map<String, EntityConfig> config = ConfigDirectory.parseEntityConfigsFromResource("classpath:/org/commacq/db/groups-sql");
		
		assertEquals(1, config.size());
		assertTrue(config.containsKey("holidayWithGroups"));
		assertEquals(Collections.singleton("country"), config.get("holidayWithGroups").getGroups());
	}
	
	

}