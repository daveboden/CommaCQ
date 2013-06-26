package org.commacq.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;

import org.commacq.EntityConfig;
import org.junit.Test;

public class ConfigDirectoryTest {

	@Test
	public void testResourceScanning() throws IOException {
		Map<String, EntityConfig> config = ConfigDirectory.parseEntityConfigsFromResource("classpath:/org/commacq/db/test-sql");
		
		assertEquals(1, config.size());
		assertTrue(config.containsKey("example"));
	}

}