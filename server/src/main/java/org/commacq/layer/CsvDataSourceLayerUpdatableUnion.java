package org.commacq.layer;

import java.util.Collection;

import javax.annotation.concurrent.Immutable;

import org.commacq.CsvLine;
import org.commacq.CsvUpdatableLayer;
import org.commacq.CsvUpdateBlockException;
import org.springframework.jmx.export.annotation.ManagedOperation;

@Immutable
public class CsvDataSourceLayerUpdatableUnion extends CsvDataSourceLayerUnion implements CsvUpdatableLayer {
	
	public CsvDataSourceLayerUpdatableUnion(Collection<CsvUpdatableLayer> collection) {
		super(collection);
	}

	@Override
	public String pokeCsvEntry(String entityId, String id) throws CsvUpdateBlockException {
		return ((CsvUpdatableLayer)getLayer(entityId)).pokeCsvEntry(entityId, id); 
	}
	
	@Override
	@ManagedOperation
	public void reload(String entityId) throws CsvUpdateBlockException {
		CsvUpdatableLayer layer = (CsvUpdatableLayer)mapping.get(entityId);
		layer.reload(entityId);
	}
	
	@Override
	@ManagedOperation
	public void reloadAll() throws CsvUpdateBlockException {
		for(String entityId : entityIds) {
			reload(entityId);
		}
	}

	@Override
	public void startBulkUpdate(String entityId, String columnNamesCsv)
			throws CsvUpdateBlockException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startBulkUpdateForGroup(String entityId, String group,
			String idWithinGroup) throws CsvUpdateBlockException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startUpdateBlock(String entityId, String columnNamesCsv)
			throws CsvUpdateBlockException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processUpdate(String entityId, String columnNamesCsv,
			CsvLine csvLine) throws CsvUpdateBlockException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processRemove(String entityId, String id)
			throws CsvUpdateBlockException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() throws CsvUpdateBlockException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finish() throws CsvUpdateBlockException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateUntrusted(String entityId, String id)
			throws CsvUpdateBlockException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateUntrusted(String entityId, Collection<String> ids)
			throws CsvUpdateBlockException {
		// TODO Auto-generated method stub
		
	}
	
}
