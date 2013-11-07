package org.commacq;

import java.util.Collection;

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
public abstract class CsvUpdatableDataSourceBase implements CsvUpdatableDataSource {

	private final CsvLineCallbackComposite composite = new CsvLineCallbackComposite();

    @Override
    public final void getAllCsvLinesAndSubscribe(CsvLineCallback callback) {
    	composite.addCallback(callback);
    	//TODO how do we protect against updates going to the new subscriber before all the results
    	//have been fetched?
    	getAllCsvLines(callback);
    }
    
    @Override
    public final void subscribe(CsvLineCallback callback) {
    	composite.addCallback(callback);
    }
    
    @Override
    public final void unsubscribe(CsvLineCallback callback) {
    	composite.removeCallback(callback);
    }
    
    @Override
	public void startBulkUpdate(String columnNamesCsv) throws CsvUpdateBlockException {
		composite.startBulkUpdate(columnNamesCsv);
		getAllCsvLines(composite);
	}

	@Override
	public void startBulkUpdateForGroup(String group, String idWithinGroup) throws CsvUpdateBlockException {
		//By definition an "untrusted" update. The only information that the update contains is the
		//identifier within the group that needs to be reloaded.
		composite.startBulkUpdateForGroup(group, idWithinGroup);
		getCsvLinesForGroup(group, idWithinGroup, composite);
	}

	@Override
	public void startUpdateBlock(String columnNamesCsv) throws CsvUpdateBlockException {
		composite.startUpdateBlock(columnNamesCsv);		
	}

	@Override
	public void finishUpdateBlock() throws CsvUpdateBlockException {
		composite.finishUpdateBlock();
	}
	
	@Override
	public void cancel() {
		composite.cancel();
	}

	@Override
	public void processUpdate(String columnNamesCsv, CsvLine csvLine) throws CsvUpdateBlockException {
		composite.processUpdate(columnNamesCsv, csvLine);
	}

	@Override
	public void processRemove(String id) throws CsvUpdateBlockException {
		composite.processRemove(id);
	}
    
	/**
	 * TODO optionally allow a reconcile mode
	 */
    @Override
    public void updateUntrusted(String id) {
    	getCsvLine(id, composite);
    }
    
    @Override
    public void updateUntrusted(Collection<String> ids) {
    	getCsvLines(ids, composite);
    }
    
    @Override
    public void reload() throws CsvUpdateBlockException {
    	String columnNamesCsv = getColumnNamesCsv();
    	composite.startUpdateBlock(columnNamesCsv);
    	composite.startBulkUpdate(columnNamesCsv);
    	getAllCsvLines(composite);
    	composite.finishUpdateBlock();
    }

}
