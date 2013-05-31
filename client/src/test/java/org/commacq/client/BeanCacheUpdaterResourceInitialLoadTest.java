package org.commacq.client;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;

import org.junit.Test;
import org.springframework.core.io.UrlResource;

public class BeanCacheUpdaterResourceInitialLoadTest {

	@Test
	public void test1SecondTimeout() throws MalformedURLException {
		long startNanos = System.nanoTime();
		CsvToBeanStrategy<Object> csvToBeanStrategy = null; //No need for a strategy - timeout before we get to that
		try {
			new BeanCacheUpdaterResourceInitialLoad<>("customer", new UrlResource("http://localhost:12345/"), csvToBeanStrategy);
			fail("Server doesn't exist, client should throw an exception after timing out.");
		} catch(RuntimeException ex) {
			long endNanos = System.nanoTime();
			
			assertTrue("Time taken was just over a second", endNanos - startNanos >= 1000000000l);
			assertTrue("Time taken was under 2 seconds", endNanos - startNanos < 2000000000l);
		}
	}

}
