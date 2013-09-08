package org.commacq.db;

import java.util.Collections;
import java.util.Set;

import lombok.Data;

@Data
public final class EntityConfig {
	
	private final String entityId;
	private final String sql;
	private final Set<String> groups;
	
	public EntityConfig(String entityId, String sql, Set<String> groups) {
		this.entityId = entityId;
		this.sql = sql;
		this.groups = groups;
	}
	
	@SuppressWarnings("unchecked")
	public EntityConfig(String entityId, String sql) {
		this(entityId, sql, Collections.EMPTY_SET);
	}
	
}
