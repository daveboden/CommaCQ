package org.commacq.layer;

import java.util.Collection;

import org.commacq.CsvLine;
import org.commacq.CsvUpdateBlockException;

/**
 * Handles the case where the CsvDataSource is at the source end of a chain
 * and is responsible for generating the Csv lines itself.
 * 
 * Not suitable for caches, which fetch the real data from an underlying source.
 * 
 * All methods are marked as final. If slightly different behaviour is required from the CsvDataSource
 * then it should implement CsvUpdatableDataSource directly and define the behaviour. It can still
 * make use of the CsvSubscriptionHelper in that case.
 */
public abstract class AbstractUpdatableLayer extends AbstractSubscribeLayer implements UpdatableLayer {

	/**
	 * Used with a trusted update
	 */
    @Override
	public void startBulkUpdate(String entityId, String columnNamesCsv) throws CsvUpdateBlockException {
		composite.startBulkUpdate(entityId, columnNamesCsv);
	}

	@Override
	public void startBulkUpdateForGroup(String entityId, String group, String idWithinGroup) throws CsvUpdateBlockException {
		composite.startBulkUpdateForGroup(entityId, group, idWithinGroup);
	}

	@Override
	public void start(Collection<String> entityIds) throws CsvUpdateBlockException {
		composite.start(entityIds);
	}	
	
	@Override
	public void finish() throws CsvUpdateBlockException {
		composite.finish();
	}
	
	@Override
	public void cancel() {
		composite.cancel();
	}

	@Override
	public void processUpdate(String entityId, String columnNamesCsv, CsvLine csvLine) throws CsvUpdateBlockException {
		composite.processUpdate(entityId, columnNamesCsv, csvLine);
	}

	@Override
	public void processRemove(String entityId, String columnNamesCsv, String id) throws CsvUpdateBlockException {
		composite.processRemove(entityId, columnNamesCsv, id);
	}
    
	/**
	 * TODO optionally allow a reconcile mode
	 */
    @Override
    public void updateUntrusted(String entityId, String id) {
    	getCsvLine(entityId, id, composite);
    }
    
    @Override
    public void updateUntrusted(String entityId, Collection<String> ids) {
    	getCsvLines(entityId, ids, composite);
    }
    
    @Override
    public void reload(String entityId) throws CsvUpdateBlockException {
    	String columnNamesCsv = getCsvDataSource(entityId).getColumnNamesCsv();
    	composite.startBulkUpdate(entityId, columnNamesCsv);
    	getCsvDataSource(entityId).getAllCsvLines(composite);
    }

}
