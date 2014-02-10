package org.commacq.client.csvtobean.xml;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.commacq.CsvLineCallbackListImpl;
import org.commacq.CsvTextBlockToCallback;
import org.commacq.client.CsvToBeanStrategy;
import org.junit.Test;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

public class JaxbAttributeWriterStrategyPerformanceTest {
	
	@Test
	public void testJaxbWriterPerformance() throws Exception {
		
		final int NUM_OF_NAMES = 20;
		
		File csvFile = File.createTempFile("largeNumberOfAnnotations", ".csv");
		CsvListWriter out = new CsvListWriter(new FileWriter(csvFile), CsvPreference.STANDARD_PREFERENCE);
		
		List<String> columns = new ArrayList<String>();
		columns.add("id");
		for(int i = 0; i < NUM_OF_NAMES; i++) {
			columns.add("name" + i);
		}
		
		out.write(columns);
		
		columns.clear();
		
		for(int i = 0; i < 100_000; i++) {
			columns.add(String.valueOf(i));
			columns.add("constantValueForName0");
			for(int j = 1; j < NUM_OF_NAMES; j++) {
				columns.add("valueForName" + j + "_" + i);
			}			
			out.write(columns);
			columns.clear();
		}
		
		out.flush();
		out.close();
		System.out.println(csvFile.toString());
		
		JaxbAttributeWriterStrategy strategy = new JaxbAttributeWriterStrategy();
		
		String content = IOUtils.toString(csvFile.toURI().toURL());
		
		Map<String, BeanWithLargeNumberOfXmlAnnotations> output = getBeans(BeanWithLargeNumberOfXmlAnnotations.class, strategy, content);
		
		assertEquals("valueForName7_3", output.get("3").getName7());
	}
	
	private <BeanType> Map<String, BeanType> getBeans(Class<BeanType> beanType, CsvToBeanStrategy strategy, String csvHeaderAndBody) {
		CsvTextBlockToCallback csvTextBlockToCallback = new CsvTextBlockToCallback();
		CsvLineCallbackListImpl callbackListImpl = new CsvLineCallbackListImpl();
		csvTextBlockToCallback.presentTextBlockToCsvLineCallback("testEntity", csvHeaderAndBody, callbackListImpl);
		return strategy.getBeans(beanType, callbackListImpl.getColumnNamesCsv(), callbackListImpl.getUpdateList());
	}
	
}
