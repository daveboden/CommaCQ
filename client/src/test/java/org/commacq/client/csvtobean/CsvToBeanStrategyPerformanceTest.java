package org.commacq.client.csvtobean;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.commacq.CsvLine;
import org.commacq.CsvTextBlockToCallback;
import org.commacq.CsvUpdateBlockException;
import org.commacq.LineCallback;
import org.commacq.client.CsvToBeanStrategy;
import org.commacq.client.CsvToBeanStrategySpringConstructor;
import org.commacq.client.csvtobean.xml.JaxbAttributeWriterStrategy;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

/**
 * Run this performance test in a very memory-constrained environment (e.g. 2mb heap!)
 * to ensure that nothing is getting built up that can't be garbage collected.
 * 
 * A large amount of data is streamed out to a file then streamed back in and processed.
 */
public class CsvToBeanStrategyPerformanceTest {
	
	private static File csvFile;
	
	@BeforeClass
	public static void createPerformanceFile() throws Exception {
		final int NUM_OF_NAMES = 20;
		
		csvFile = File.createTempFile("largeNumberOfAnnotations", ".csv");
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
	}
	
	@Test
	public void testJaxbWriterPerformance() throws Exception {
		long milliseconds = testWriterPerformance(new JaxbAttributeWriterStrategy());
		System.out.println("Jaxb took " + milliseconds + " milliseconds");
	}
	
	@Ignore //Not performant. Needs significant work.
	@Test
	public void testSpringConstructorWriterPerformance() throws Exception {
		long milliseconds = testWriterPerformance(new CsvToBeanStrategySpringConstructor());
		System.out.println("Spring Constructor took " + milliseconds + " milliseconds");
	}
	
	/**
	 * @return time taken in milliseonds
	 */
	public long testWriterPerformance(final CsvToBeanStrategy strategy) throws Exception {

		FileReader csvFileReader = new FileReader(csvFile);

		CsvTextBlockToCallback textBlockToCallback = new CsvTextBlockToCallback();
		final AtomicInteger rowCount = new AtomicInteger();
		
		long timeBefore = System.nanoTime();
		textBlockToCallback.presentTextBlockToCsvLineCallback("testEntity", csvFileReader, new LineCallback() {
			
			@Override
			public void processUpdate(String entityId, String columnNamesCsv, CsvLine csvLine) throws CsvUpdateBlockException {
				BeanWithLargeNumberOfAttributes bean = strategy.getBean(BeanWithLargeNumberOfAttributes.class, columnNamesCsv, csvLine);
				int currentRow = rowCount.getAndIncrement();
				if(currentRow == 3) {
					assertEquals("valueForName7_3", bean.getName7());
				}
			}
			
			@Override
			public void processRemove(String entityId, String columnNamesCsv, String id) throws CsvUpdateBlockException {
				throw new UnsupportedOperationException();
			}
		});
		
		long timeAfter = System.nanoTime() - timeBefore;
		
		assertEquals(100_000, rowCount.get());
		
		return timeAfter / 1_000_000;
	}
	
}
