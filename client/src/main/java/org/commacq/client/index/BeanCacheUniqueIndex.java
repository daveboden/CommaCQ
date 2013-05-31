package org.commacq.client.index;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class BeanCacheUniqueIndex<KeyType,BeanType> {

	private static final Logger logger = LoggerFactory.getLogger(BeanCacheUniqueIndex.class);
	
	//Store the definition to be used during updates
	private final BeanCacheIndexDefinition<KeyType, BeanType> def;
	
	private final BiMap<KeyType, String> index;
	
	public BeanCacheUniqueIndex(BeanCacheIndexDefinition<KeyType, BeanType> def, Map<String, BeanType> items) {
		assert def.isUnique();
		this.def = def;
		index = HashBiMap.create();
		
		for(Entry<String, BeanType> item : items.entrySet()) {
			KeyType key = def.getKeyForValue(item.getValue());
			
			index.put(key, item.getKey());
		}
	}

	public String getForKey(KeyType key) {
		return index.get(key);
	}
	
    public String mustGetForKey(KeyType key) {
        String id = index.get(key);
        if(id == null) {
            throw new RuntimeException("No entry in index corresponding to: " + key);
        }
        return id;
    }
    
    //TODO consider synchronization
    public void update(Map<String, BeanType> items) {
    	//TODO Is there a collection type which will allow reverse lookups of id to key?
    	//Until there is, we're forced to iterate around the whole value set.
    	
		for(Entry<String, BeanType> item : items.entrySet()) {
			String currentIdToReplace = item.getKey();
			
			KeyType removedIndexKey = index.inverse().remove(currentIdToReplace);
			logger.debug("Removed existing index mapping for bean id {} against key {}",
					     currentIdToReplace, removedIndexKey);
			
			KeyType key = def.getKeyForValue(item.getValue());
			logger.debug("Adding entry into index for bean id {} for key {}",
					     currentIdToReplace, key);
			index.put(key, currentIdToReplace);
		}
    }
    
    //TODO consider synchronization
    public void delete(Set<String> ids) {
    	for(String currentIdToReplace : ids) {
    		KeyType removedIndexKey = index.inverse().remove(currentIdToReplace);
    		logger.debug("Removed existing index mapping for bean id {} against key {}",
    				     currentIdToReplace, removedIndexKey);
    	}
    }
	
}