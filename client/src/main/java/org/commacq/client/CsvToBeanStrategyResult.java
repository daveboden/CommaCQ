package org.commacq.client;

import java.util.Map;
import java.util.Set;

/**
 * Report beans updated (inserted/updated) and beans deleted
 */
public class CsvToBeanStrategyResult<BeanType> {

	private final Map<String, BeanType> updated;
	private final Set<String> deleted;

	public CsvToBeanStrategyResult(Map<String, BeanType> updated,
			Set<String> deleted) {
		this.updated = updated;
		this.deleted = deleted;
	}

	public Map<String, BeanType> getUpdated() {
		return updated;
	}
	
	public Set<String> getDeleted() {
		return deleted;
	}
	
}