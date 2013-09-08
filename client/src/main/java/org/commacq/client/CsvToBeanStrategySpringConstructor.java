package org.commacq.client;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVParser;
import org.commacq.CsvLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.support.GenericXmlApplicationContext;

/**
 * Converts fully formed CSV to a map of beans. The CSV must have the header row on it.
 * 
 * Uses constructor named arguments and spring conversion to
 * convert the column names and values into a constructor call. This
 * allows it to construct strictly immutable beans using the bean's
 * constructor. Has no need for a private no-args constructor or any
 * setters in the bean type.
 * 
 * NOT THREADSAFE
 */
public class CsvToBeanStrategySpringConstructor implements CsvToBeanStrategy {
	
	Logger logger = LoggerFactory.getLogger(CsvToBeanStrategySpringConstructor.class);
	
	private final GenericXmlApplicationContext context;

	public CsvToBeanStrategySpringConstructor() {
		context = new GenericXmlApplicationContext("classpath:org/commacq/client/csv/csvConversionService.xml");
	}
	
	//TODO Totally inefficient, converts from string to csv array back to string and back to csv array again.
	@Override
	public <BeanType> BeanType getBean(Class<BeanType> beanType, String columnNamesCsv, CsvLine csvLine) {
		CSVParser parser = new CSVParser(new StringReader(columnNamesCsv));
		String[] columnNames;
		try {
			columnNames = parser.getLine();
		} catch(IOException ex) {
			throw new RuntimeException("Error parsing CSV column names: " + columnNamesCsv);
		}
		
		parser = new CSVParser(new StringReader(csvLine.getCsvLine()));
		
		String[] values;
		try {
			 values = parser.getLine();
		} catch (IOException ex) {
			throw new RuntimeException("Error parsing CSV row: " + csvLine.getCsvLine());
		}
		
		Map<String, BeanType> beans = getBeans(beanType, Arrays.asList(columnNames), Collections.singletonMap(csvLine.getId(), Arrays.asList(values)));
		return beans.get(csvLine.getId());
	}
	
	@Override
	public <BeanType> Map<String, BeanType> getBeans(Class<BeanType> beanType, String columnNamesCsv, Collection<CsvLine> csvLines) {
		Map<String, BeanType> beansMap = new HashMap<>(csvLines.size());
		for(CsvLine csvLine : csvLines) {
			beansMap.put(csvLine.getId(), getBean(beanType, columnNamesCsv, csvLine));
		}
		return beansMap;
	}
	
	@Override
	public <BeanType> Map<String, BeanType> getBeans(Class<BeanType> beanType, List<String> columnNames, Map<String, List<String>> body) {

		final ConstructorArgumentValues cav = new ConstructorArgumentValues();
		
		final GenericBeanDefinition beanDef = new GenericBeanDefinition();
		beanDef.setBeanClass(beanType);
		
		logger.info("Parsing CSV; column names are {}", columnNames);

		Map<String, BeanType> beans = new HashMap<>(body.size());
		
		for(Entry<String, List<String>> lineEntry : body.entrySet()) {
			List<String> line = lineEntry.getValue();
			
			for(int i = 0; i < columnNames.size(); i++) {
				String columnName = columnNames.get(i);
				String value = line.get(i);
				
				//From SimpleConstructorNamespaceHandler
				ConstructorArgumentValues.ValueHolder valueHolder = new ValueHolder(value);
				valueHolder.setName(columnName);
				cav.addGenericArgumentValue(valueHolder);
			}
			beanDef.setConstructorArgumentValues(cav);
			
			context.registerBeanDefinition("bean", beanDef);
			@SuppressWarnings("unchecked")
			BeanType bean = (BeanType)context.getBean("bean");
			context.removeBeanDefinition("bean");
			
			cav.clear(); //Ready for next row
			
			beans.put(line.get(0), bean); //id is always first column			
		}
		
		logger.info("Updated {} beans", beans.size());
		logger.debug("Updated beans {}", beans); //Potentially very slow logging operation

		return beans; 
	}
	
}