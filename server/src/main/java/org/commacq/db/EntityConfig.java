package org.commacq.db;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public final class EntityConfig {
	
	private final String entityId;
	private final String sql;
	private final Set<String> groups;
	private final List<String> compositeIdColumns;
	
	@SuppressWarnings("unchecked")
	public EntityConfig(String entityId, String sql) {
		this(entityId, sql, Collections.EMPTY_SET, null);
	}
	
	public EntityConfig(String entityId, String sql, Set<String> groups) {
		this(entityId, sql, groups, null);
	}
	
}
