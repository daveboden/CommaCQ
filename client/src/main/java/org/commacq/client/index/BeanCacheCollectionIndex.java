package org.commacq.client.index;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;


/**
 * An index definition that allows duplicate entries for the
 * same key. Represents the duplicate entries as a collection
 * of id entries. Each id then needs to be looked up on the
 * relevant manager individually. We have to do things this way
 * because there's no responsibility on a bean to know its own
 * identifier!
 */
//TODO, this class is likely to be very slow
public class BeanCacheCollectionIndex<KeyType, BeanType> {

	private static final Logger logger = LoggerFactory.getLogger(BeanCacheCollectionIndex.class);
	
	//Store the definition to be used during updates
	private final BeanCacheIndexDefinition<KeyType, BeanType> def;
	
	private final SetMultimap<KeyType, String> index;
	
	public BeanCacheCollectionIndex(BeanCacheIndexDefinition<KeyType, BeanType> def, Map<String, BeanType> items) {
		this(def, items, HashMultimap.<KeyType, String>create());
	}
	
	public BeanCacheCollectionIndex(BeanCacheIndexDefinition<KeyType, BeanType> def, Map<String, BeanType> items, int numberOfKeysHint) {
		this(def,
			 items,
			 HashMultimap.<KeyType, String>create(numberOfKeysHint, items.entrySet().size() / numberOfKeysHint)
		);
	}
	
	private BeanCacheCollectionIndex(BeanCacheIndexDefinition<KeyType, BeanType> def, Map<String, BeanType> items, SetMultimap<KeyType, String> index) {
		assert !def.isUnique();
		this.def = def;
		this.index = index;
		
		for(Entry<String, BeanType> item : items.entrySet()) {
			KeyType key = def.getKeyForValue(item.getValue());
			
			index.put(key, item.getKey());
		}
	}

	public Collection<String> getForKey(KeyType key) {
		return index.get(key);
	}
	
    public Collection<String> mustGetForKey(KeyType key) {
        Collection<String> ids = index.get(key);
        if(ids.isEmpty()) {
            throw new RuntimeException("No entries in index corresponding to: " + key);
        }
        return ids;
    }
    
    
    private void removeId(String id) {
		for(Entry<KeyType, String> entry : index.entries()) {
			if(entry.getValue().equals(id)) {
				logger.debug("Located and removing mapping for bean id {} against key {}",
						     id, entry.getKey());
				index.remove(entry.getKey(), entry.getValue());
				break;
			}
		}
    }
    
    //TODO consider synchronization
    public void update(Map<String, BeanType> items) {
    	//TODO Is there a collection type which will allow reverse lookups of ids to keys?
    	//Until there is, we're forced to iterate around the whole set.
    	
		for(Entry<String, BeanType> item : items.entrySet()) {			
			String currentIdToReplace = item.getKey();
			logger.debug("Removing existing index mapping for bean id {}", currentIdToReplace); 
			removeId(currentIdToReplace);
			
			KeyType key = def.getKeyForValue(item.getValue());
			logger.debug("Adding entry into index for bean id {} for key {}",
					     currentIdToReplace, key);
			index.put(key, currentIdToReplace);
		}
    }
    
    //TODO consider synchronization
    public void delete(Set<String> ids) {
    	for(String currentIdToReplace : ids) {
			logger.debug("Removing existing index mapping for bean id {}", currentIdToReplace); 
			removeId(currentIdToReplace);
    	}
    }
	
}