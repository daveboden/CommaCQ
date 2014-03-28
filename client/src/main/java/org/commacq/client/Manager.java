package org.commacq.client;

import java.util.Map;

import org.commacq.CompositeIdEncoding;
import org.commacq.CompositeIdEncodingEscaped;

/**
 * The user may choose to extend the Manager type to add useful methods
 * (e.g. getActive(id) and mustGetActive(id) which introduce the concept
 * of a bean that can be active or inactive and allows the user only
 * to get active instances).
 * 
 * However, it is not encouraged to extend the manager just in order
 * name the manager and make it 'easier' to autowire. Autowiring will be helped
 * by convention. A manager for an entity called "product" will be, by default,
 * given an identifier of "productManager". By using this name for the field
 * name, the user will find that the correct manager is automatically injected
 * into a declaration: @Resource private Manager<Product> productManager;
 * If the user wishes to name the field differently, or give the manager a different
 * resource (Spring) identifier, then @Resource(name=...) will make the autowiring
 * pick the correct resource.
 * 
 * Responsible for initialising the bean cache on construction so that as soon
 * as the client gets the manager, it's ready.
 * @param <BeanType>
 */
public class Manager<BeanType> {
	
	private final Class<BeanType> beanType;
	private final String entityId;
	
	//Will be a ConcurrentHashMap in normal operation or a
	//copy (HashMap) if in snapshot operation.
	protected final BeanCache<BeanType> beanCache;
	
	protected CompositeIdEncoding compositeIdEncoding;
	
	/**
	 * @param beanType
	 * @param entityId The server knows the entity by this name
	 */
	public Manager(BeanCache<BeanType> beanCache) {
		this.beanCache = beanCache;
		this.beanType = beanCache.getBeanType();
		this.entityId = beanCache.getEntityId();
		
		this.compositeIdEncoding = new CompositeIdEncodingEscaped();
	}
	
	/**
	 * For use with getSnapshot()
	 */
	/*
	protected Manager(Class<BeanType> beanType, String entityId, Map<String, BeanType> beanCache) {
		this.beanType = beanType;
		this.entityId = entityId;
		this.beanCache = null;
		
		//shallow copy that doesn't have an updater associated
		//with it and therefore does not need to support concurrency
		this.beanCache = new HashMap<String, BeanType>(beanCache.getAllMappings()); 
	}
	*/

	/**
	 * Returns the bean using its identifier, or null if the bean is not
	 * present in the cache. Most of the time you'll want to use mustGet()
	 * to avoid having to check for null values.
	 */
	public BeanType get(String id) {
		return beanCache.get(id);
	}
	
	public BeanType getByCompositeId(String... idComponents) {
		return get(compositeIdEncoding.createCompositeId(idComponents));
	}
	
	/**
	 * Returns the bean using its identifier, or throws a RuntimeException if
	 * the bean is not present in the cache.
	 */
	public BeanType mustGet(String id) {
		BeanType bean = beanCache.get(id);
		if(bean == null) {
			throw new RuntimeException(String.format("Could not get bean %s for entity %s of type %s",
					                                 id, entityId, beanType.getName()));
		}
		return bean;
	}
	
	public BeanType mustGetByCompositeId(String... idComponents) {
		return mustGet(compositeIdEncoding.createCompositeId(idComponents));
	}
	
	/**
	 * It's not enough to return all beans, because the beans don't necessarily
	 * know their identifiers. Most of them surely will, but theres no strict responsibility
	 * (otherwise we'd have to make all beans implement a getId() interface.
	 * Therefore, this method returns a map of identifiers to beans.
	 */
	protected Map<String, BeanType> getAllMappings() {
		return beanCache.getAllMappings();
	}
	
	/**
	 * Returns a copy of the manager with a stable, immutable bean cache that
	 * won't receive any updates.
	 * Makes sure that you don't get any updates half way through a transaction
	 * where you want your copy of the data to be consistent throughout it.
	 * Do not hold a reference to this snapshot for longer than a single
	 * atomic transaction. Release it and let it be garbage collected once
	 * you've finished with it.
	 */
	/*
	public final Manager<BeanType> getSnapshot() {
		return new Manager<BeanType>(this.beanType, this.entityId, this.beanCache);
	}
	*/
	
	public Class<BeanType> getBeanType() {
		return beanType;
	}
	
	public void addCacheObserver(CacheObserver<BeanType> cacheObserver) {
		if(beanCache == null) {
			throw new UnsupportedOperationException("Cannot register an observer on a snapshot");	
		}
		beanCache.addCacheObserver(cacheObserver);
	}
	
	public void removeCacheObserver(CacheObserver<BeanType> cacheObserver) {
		if(beanCache == null) {
			throw new UnsupportedOperationException("Cannot remove an observer on a snapshot");	
		}
		beanCache.removeCacheObserver(cacheObserver);
	}
	
}
