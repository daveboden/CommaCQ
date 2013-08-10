package org.commacq.client;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVParser;
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
public class CsvToBeanStrategySpringConstructor<BeanType> implements CsvToBeanStrategy<BeanType> {
	
	Logger logger = LoggerFactory.getLogger(CsvToBeanStrategySpringConstructor.class);
	
	private final Class<BeanType> beanType;
	private final GenericBeanDefinition beanDef;
	private final GenericXmlApplicationContext context;
	private final ConstructorArgumentValues cav = new ConstructorArgumentValues();

	public CsvToBeanStrategySpringConstructor(Class<BeanType> beanType) {
		this.beanType = beanType;
		beanDef = new GenericBeanDefinition();
		beanDef.setBeanClass(beanType);
		
		context = new GenericXmlApplicationContext("classpath:org/commacq/client/csv/csvConversionService.xml");
	}
	
	@Override
	public CsvToBeanStrategyResult<BeanType> getBeans(String csvHeaderAndBody) {
		 Reader in = new StringReader(csvHeaderAndBody);
		 String[][] csv;
		 try {
			 csv = new CSVParser(in).getAllValues();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		 
		Map<String, BeanType> beans = new HashMap<>(csv.length);
		Set<String> deleted = new HashSet<>();
		
		List<String> columnNames = Arrays.asList(csv[0]);
		logger.info("Parsing CSV; column names are {}", columnNames);
		
		for(int lineIndex = 1; lineIndex < csv.length; lineIndex++) {
			List<String> line = Arrays.asList(csv[lineIndex]);
			
			if(line.size() == 1) {
				deleted.add(line.get(0));
				continue;
			}
			
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
		if(logger.isDebugEnabled()) {
			if(!deleted.isEmpty()) {
				logger.debug("Deleted ids {}", deleted);
			}
		}
		return new CsvToBeanStrategyResult<>(beans, deleted); 
	}
	
	@Override
	public Class<BeanType> getBeanType() {
		return beanType;
	}
	
}