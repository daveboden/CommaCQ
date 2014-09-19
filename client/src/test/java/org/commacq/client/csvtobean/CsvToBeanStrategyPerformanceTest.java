package org.commacq.client.csvtobean;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.commacq.CsvLine;
import org.commacq.CsvTextBlockToCallback;
import org.commacq.CsvUpdateBlockException;
import org.commacq.LineCallback;
import org.commacq.client.CsvToBeanStrategy;
import org.commacq.client.CsvToBeanStrategySpringConstructor;
import org.commacq.client.csvtobean.constructorparams.ConstructorParamsStrategy;
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
	private static final int NUM_OF_NAMES = 20;
	
	@BeforeClass
	public static void createPerformanceFile() throws Exception {
		
		csvFile = File.createTempFile("largeNumberOfAnnotations", ".csv");
		CsvListWriter out = new CsvListWriter(new FileWriter(csvFile), CsvPreference.STANDARD_PREFERENCE);
		
		String[] columns = new String[NUM_OF_NAMES + 1];

		populateColumnHeadings(columns);
		out.write(columns);
		
		for(int i = 0; i < 100_000; i++) {
			populateValuesForLine(i, columns);
			out.write(columns);
		}
		
		out.flush();
		out.close();
		System.out.println(csvFile.toString());
	}
	
	private static void populateColumnHeadings(String[] columns) {
		columns[0] = "id";
		for(int i = 0; i < NUM_OF_NAMES; i++) {
			columns[i + 1] = "name" + i;
		}		
	}
	
	/**
	 * Always populates all values of columns, so no need to clear array between
	 * uses.
	 * @param lineNumber
	 * @param columns
	 */
	private static void populateValuesForLine(int lineNumber, String[] columns) {
		columns[0] = (String.valueOf(lineNumber));
		columns[1] = "constantValueForName0";
		for(int j = 1; j < NUM_OF_NAMES; j++) {
			columns[j+1] = "valueForName" + j + "_" + lineNumber;
		}
	}
	
	@Test
	public void testJaxbWriterPerformance() throws Exception {
		long milliseconds = testWriterPerformance(new JaxbAttributeWriterStrategy());
		System.out.println("Jaxb took " + milliseconds + " milliseconds");
	}
	
	@Test
	public void testJava8ConstructorParamsPerformance() throws Exception {
		long milliseconds = testWriterPerformance(new ConstructorParamsStrategy());
		System.out.println("Java 8 Constructor took " + milliseconds + " milliseconds");
	}
	
	/**
	 * Supplies 100,000 sets of parameters to the Jaxb object creation binder and
	 * sees how quickly the objects are created.
	 * @throws Exception
	 */
	@Test
	public void testJaxbWriterRaw() throws Exception {
		JaxbAttributeWriterStrategy strategy = new JaxbAttributeWriterStrategy();
		
		final String[] columnHeadings = new String[NUM_OF_NAMES + 1];
		populateColumnHeadings(columnHeadings);
		final List<String> columnHeadingsList = Arrays.asList(columnHeadings);
		
		String[] rowValues = new String[NUM_OF_NAMES + 1];
		final List<String> rowValuesList = Arrays.asList(rowValues);
		long timeBefore = System.nanoTime();
		for(int i = 0; i < 100_000; i++) {
			populateValuesForLine(i, rowValues);
			strategy.getBean(BeanWithLargeNumberOfAttributes.class, columnHeadingsList, rowValuesList);
		}
		long timeTaken = System.nanoTime() - timeBefore;
		long timeTakenInMillis =  timeTaken / 1_000_000;
		
		System.out.println("Jaxb Writer Raw took " + timeTakenInMillis + " milliseconds");
	}
	
	/**
	 * Supplies 100,000 sets of parameters to the Java 8 Constructor and sees how
	 * quickly the objects are created.
	 */
	@Test
	public void testJava8ConstructorParamsRaw() throws Exception {
		ConstructorParamsStrategy strategy = new ConstructorParamsStrategy();
		Constructor<?> con = strategy.chooseConstructor(BeanWithLargeNumberOfAttributes.class);
		
		String[] columns = new String[NUM_OF_NAMES + 1];
		long timeBefore = System.nanoTime();
		for(int i = 0; i < 100_000; i++) {
			populateValuesForLine(i, columns);
			strategy.getBean(BeanWithLargeNumberOfAttributes.class, con, columns);
		}
		long timeTaken = System.nanoTime() - timeBefore;
		long timeTakenInMillis =  timeTaken / 1_000_000;
		
		System.out.println("Java 8 Constructor Raw took " + timeTakenInMillis + " milliseconds");
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

		Reader csvFileReader = new BufferedReader(new FileReader(csvFile), 5_000_000);

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
		
		long timeTaken = System.nanoTime() - timeBefore;
		
		assertEquals(100_000, rowCount.get());
		
		return timeTaken / 1_000_000;
	}
	
}
