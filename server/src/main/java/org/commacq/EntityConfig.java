package org.commacq;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class EntityConfig {
	
	private String entityId;
	private String sql;
	
	public EntityConfig(String entityId, String sql) {
		this.entityId = entityId;
		this.sql = sql;		
	}
	
	public String getEntityId() {
        return entityId;
    }

    public String getSql() {
		return sql;
	}
	
}