package org.commacq.client;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.NotThreadSafe;

import lombok.extern.slf4j.Slf4j;

import org.commacq.CsvLine;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

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
@NotThreadSafe
@Slf4j
public class CsvToBeanStrategySpringConstructor implements CsvToBeanStrategy {
	
	private final GenericXmlApplicationContext context;
	private final ConstructorArgumentValues cav = new ConstructorArgumentValues();
	private final GenericBeanDefinition beanDef = new GenericBeanDefinition();
	
	private final CsvListReaderUtil csvListReaderUtil = new CsvListReaderUtil();

	public CsvToBeanStrategySpringConstructor() {
		context = new GenericXmlApplicationContext("classpath:org/commacq/client/csv/csvConversionService.xml");
		beanDef.setConstructorArgumentValues(cav);
	}
	
	private List<String> splitCsv(String columnNamesCsv) {
		try {
			csvListReaderUtil.appendLine(columnNamesCsv);
			return csvListReaderUtil.getParser().read();
		} catch(IOException ex) {
			throw new RuntimeException("Error parsing CSV column names: " + columnNamesCsv);
		}
	}
	
	@Override
	public <BeanType> BeanType getBean(Class<BeanType> beanType, String columnNamesCsv, CsvLine csvLine) {
		return getBeans(beanType, columnNamesCsv, Collections.singleton(csvLine)).get(csvLine.getId());
	}
	
	/**
	 * TODO More efficient to pass in a block of text rather than a collection of CsvLines?
	 */
	@Override
	public <BeanType> Map<String, BeanType> getBeans(Class<BeanType> beanType, String columnNamesCsv, Collection<CsvLine> csvLines) {

		List<String> columnNames = splitCsv(columnNamesCsv);
		
		beanDef.setBeanClass(beanType);
		
		//log.debug("Parsing CSV; column names are {}", columnNamesCsv);

		Map<String, BeanType> beans = new HashMap<>(csvLines.size());
		
		for(CsvLine csvLine : csvLines) {
			List<String> line = splitCsv(csvLine.getCsvLine());
			
			for(int i = 0; i < columnNames.size(); i++) {
				String columnName = columnNames.get(i);
				String value = line.get(i);
				
				//From SimpleConstructorNamespaceHandler
				ConstructorArgumentValues.ValueHolder valueHolder = new ValueHolder(value);
				valueHolder.setName(columnName);
				cav.addGenericArgumentValue(valueHolder);
			}
			
			context.registerBeanDefinition("bean", beanDef);
			@SuppressWarnings("unchecked")
			BeanType bean = (BeanType)context.getBean("bean");
			context.removeBeanDefinition("bean");
			
			cav.clear(); //Ready for next row
			
			beans.put(line.get(0), bean); //id is always first column			
		}
		
		//log.info("Updated {} beans", beans.size());

		return beans; 
	}
	
}