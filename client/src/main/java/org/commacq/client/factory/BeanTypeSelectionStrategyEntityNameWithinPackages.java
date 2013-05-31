package org.commacq.client.factory;

import java.util.Arrays;

import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple selection strategy which capitalises the first letter of the
 * entity name (customer becomes Customer) and then looks for the class
 * within the package specified in the constructor.
 */
public class BeanTypeSelectionStrategyEntityNameWithinPackages implements BeanTypeSelectionStrategy {
	
	Logger logger = LoggerFactory.getLogger(BeanTypeSelectionStrategyEntityNameWithinPackages.class);

	protected final String[] packageNames;

	public BeanTypeSelectionStrategyEntityNameWithinPackages(String... packageNames) {
	    this.packageNames = packageNames;
    }
	
	@Override
	public Class<?> chooseBeanType(String entityName) throws ClassNotFoundException {
		final String capitalisedEntityName = WordUtils.capitalize(entityName);
		for(String packageName : packageNames) {
			String className = packageName + "." + capitalisedEntityName;
			try {
				return Class.forName(className);
			} catch(ClassNotFoundException ex) {
				logger.debug("Did not find class: {}", className);
			}
		}
		throw new ClassCastException("Did not find class " + capitalisedEntityName +
				                     " in any of the packages:" + Arrays.toString(packageNames));
	}
	
}