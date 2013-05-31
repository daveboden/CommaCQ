package org.commacq.client.index;

/**
 * Defines an index by:
 * 
 *   Giving it a name
 *   Specifying whether a key maps to multiple values
 *   Implementing getKeyForValue(bean) which defines the reverse mapping
 *      between the bean and its index key.
 * 
 * @param <KeyType>
 * @param <BeanType>
 */
public abstract class BeanCacheIndexDefinition<KeyType, BeanType> {

	protected String name;
	protected boolean unique;
	
	public BeanCacheIndexDefinition(String name, boolean unique) {
		this.name = name;
		this.unique = unique;
    }
	
	public String getName() {
		return name;
	}
	
	public boolean isUnique() {
	    return unique;
	}
	
	/**
	 * Specify the index key which applies to this bean.
	 * If null is returned from this method, the meaning is that there is
	 * no key for this bean and therefore that this bean should not be
	 * included in the index mapping. This also allows indexes to be used
	 * to create a subset of the entire cache. For example, if we have a
	 * subset of active records that we want to index, one could return
	 * null for all inactive records. The resulting index would still 
	 * be keyed on the bean's id field but the index would only contain
	 * active records.
	 * 
	 * @return null if this bean should not form part of this index.
	 */
	public abstract KeyType getKeyForValue(BeanType bean);
}