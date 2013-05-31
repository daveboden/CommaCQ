package org.commacq.client;


/**
 * Takes a CSV header and a number of CSV rows and returns beans
 * of the specified type.
 * 
 * The csv body can contain rows with only one column populated
 * and no trailing commas. This represents an identifier that has
 * been deleted.
 */
public interface CsvToBeanStrategy<BeanType> {

	CsvToBeanStrategyResult<BeanType> getBeans(String csvHeaderAndBody);
	
	Class<BeanType> getBeanType();
	
}