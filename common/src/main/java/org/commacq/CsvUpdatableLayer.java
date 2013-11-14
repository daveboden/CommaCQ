package org.commacq;

import java.util.Collection;

import lombok.RequiredArgsConstructor;

/**
 * A CSV Data Source that can handle external updates
 * and notify subscribers.
 * 
 * @See CsvSubscriptionHelper
 */
public interface CsvUpdatableLayer extends CsvDataSourceLayer, CsvLineCallback {
	
	@RequiredArgsConstructor
	public enum UpdateMode {
		trusted("CsvLines will be published downstream without reference to the data source."),
		untrusted("The id of each CsvLine will be looked up in the data source and the result will be published downstream."),
		reconcile("The id of each CsvLine will be lookup up in the data source and the result will be published downstream. "
				+ "An error will be logged if the incoming update does not match what's in the data source.");
		
		final String description;
	}
	
	/**
	 * If the update just contains a list of ids, best to just present the ids and
	 * get the data loaded from the data source. These updates are "untrusted".
	 * 
	 * Call this as an alternative to the processUpdate and processRemove methods.
	 * 
	 * @param id
	 */
	void updateUntrusted(String entityId, String id) throws CsvUpdateBlockException;
	
	void updateUntrusted(String entityId, Collection<String> ids) throws CsvUpdateBlockException;
	
	void reload(String entityId) throws CsvUpdateBlockException;
	
	String pokeCsvEntry(String entityId, String id) throws CsvUpdateBlockException;
	void reloadAll() throws CsvUpdateBlockException;
	
}
