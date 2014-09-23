package org.commacq.client.csvtobean.constructorparams;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.commacq.CsvLine;
import org.commacq.CsvListReaderUtil;
import org.commacq.client.CsvToBeanStrategy;

public class ConstructorParamsStrategy implements CsvToBeanStrategy {
	
	protected final CsvListReaderUtil csvListReaderUtil = new CsvListReaderUtil();

	//TODO - for testing only
	Constructor<?> con;
	Map<Integer, Integer> csvColummToConstructorPositionMap;
	
	//Avoid creating a parameters array in memory each time, just to save memory footprint
	String[] parameters;
	
	@Override
	public <BeanType> BeanType getBean(Class<BeanType> beanType, String columnNamesCsv, CsvLine csvLine) {
		
		//TODO cache columnNamesCsv and throw an error if it changes. We're building lots of assumptions on it being constant.
		
		if(csvColummToConstructorPositionMap == null) {
			//@SuppressWarnings("unchecked")
			con = chooseConstructor(beanType);
			parameters = new String[con.getParameterCount()];
			//Map<Integer, Integer> csvColummToConstructorPositionMap = getCsvColummToConstructorPositionMap(con, columnNamesCsv, csvLine);
			csvColummToConstructorPositionMap = getCsvColummToConstructorPositionMap(con, columnNamesCsv, csvLine);
		}
		List<String> value = splitCsv(csvLine.getCsvLine());
		try {
			for(Entry<Integer, Integer> entry : csvColummToConstructorPositionMap.entrySet()) {
				parameters[entry.getValue()] = value.get(entry.getKey());
			}
			return getBean(beanType, con, parameters);
		} finally {
			//Always clear down the parameters array for next time
			Arrays.fill(parameters, null);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <BeanType> BeanType getBean(Class<BeanType> beanType, Constructor<?> con, Object[] parameters) {
		try {
			return (BeanType)con.newInstance((Object[])parameters);
		} catch (InstantiationException | IllegalAccessException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	public <BeanType> Constructor<?> chooseConstructor(Class<BeanType> beanType) {
		for(Constructor<?> constructor : beanType.getConstructors()) {
			if(constructor.getParameters().length != 0) {
				//TODO choose from candidate constructors rather than just returning the first non-empty constructor
				return constructor;
			}
		}
		throw new RuntimeException("No appropriate constructors on: " + beanType);
	}

	@Override
	public <BeanType> Map<String, BeanType> getBeans(Class<BeanType> beanType, String columnNamesCsv, Collection<CsvLine> csvLines) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public Map<Integer, Integer> getCsvColummToConstructorPositionMap(Constructor<?> con, String columnNamesCsv, CsvLine csvLine) {
		List<String> csvColumnNames = splitCsv(columnNamesCsv);
		
		Parameter[] params = con.getParameters();
		Map<String, Integer> paramNames = new HashMap<>();
		for(int i = 0; i < params.length; i++) {
			String name = con.getParameters()[i].getName();
			paramNames.put(name, i);
		}
		
		//Create a mapping of csv column position to constructor parameter position
		Map<Integer, Integer> csvColummToConstructorPositionMap = new HashMap<>();
		for(int i = 0; i < csvColumnNames.size(); i++) {
			String columnName = csvColumnNames.get(i);
			Integer constructorParamPosition = paramNames.get(columnName);
			if(constructorParamPosition != null) {
				csvColummToConstructorPositionMap.put(i, constructorParamPosition);
			}
		}
		
		//Validate the position map
		
		List<Integer> constructorParameterIndexes = new ArrayList<>(csvColummToConstructorPositionMap.values());
		constructorParameterIndexes.sort(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o1.compareTo(o2);
			}
		});
		
		if(constructorParameterIndexes.size() != params.length) {
			throw new IllegalArgumentException("CSV column names do not match constructor parameter names");
		}
		
		for(int i = 0; i < constructorParameterIndexes.size(); i++) {
			if(constructorParameterIndexes.get(i) != i) {
				throw new IllegalArgumentException("CSV column names do not match all constructor parameter names");	
			}
		}
		
		//end validation
		
		return csvColummToConstructorPositionMap;
	}
	
	private List<String> splitCsv(String columnNamesCsv) {
		try {
			csvListReaderUtil.appendLine(columnNamesCsv);
			return csvListReaderUtil.getParser().read();
		} catch(IOException ex) {
			throw new RuntimeException("Error parsing CSV column names: " + columnNamesCsv);
		}
	}
	
}
