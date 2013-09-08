package org.commacq.client.factory;

import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.text.WordUtils;

/**
 * Simple selection strategy which capitalises the first letter of the
 * entity name (customer becomes Customer) and then looks for the class
 * within the package specified in the constructor.
 */
@Slf4j
public class BeanTypeSelectionStrategyEntityNameWithinPackages implements BeanTypeSelectionStrategy {

	protected final String[] packageNames;

	public BeanTypeSelectionStrategyEntityNameWithinPackages(String... packageNames) {
	    this.packageNames = packageNames;
    }
	
	@Override
	public Class<?> chooseBeanType(String entityId) throws ClassNotFoundException {
		final String capitalisedEntityName = WordUtils.capitalize(entityId);
		for(String packageName : packageNames) {
			String className = packageName + "." + capitalisedEntityName;
			try {
				return Class.forName(className);
			} catch(ClassNotFoundException ex) {
				log.debug("Did not find class: {}", className);
			}
		}
		throw new ClassCastException("Did not find class " + capitalisedEntityName +
				                     " in any of the packages:" + Arrays.toString(packageNames));
	}
	
}