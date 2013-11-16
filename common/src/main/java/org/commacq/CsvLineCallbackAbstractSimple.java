package org.commacq;

import java.util.Collection;

/**
 * Implements the bulk and bulk-group methods of CsvLineCallback with
 * no behaviour. The implementations are marked as final so that a
 * developer isn't tempted to use this class then override one of the
 * methods and add behaviour. That would be confusing. A developer should
 * implement CsvLineCallback directly if bulk behaviour is required.
 */
public abstract class CsvLineCallbackAbstractSimple implements BlockCallback {

	@Override
	public final void startBulkUpdate(String entityId, String columnNamesCsv) throws CsvUpdateBlockException {
		//No behaviour defined.
	}
	
	@Override
	public final void start(Collection<String> entityIds) throws CsvUpdateBlockException {
		//No behaviour defined.
	}
	
	@Override
	public final void finish() throws CsvUpdateBlockException {
		//No behaviour defined.
	}
	
	@Override
	public final void startBulkUpdateForGroup(String entityId, String group, String idWithinGroup) throws CsvUpdateBlockException {
		//No behaviour defined.
	}
	
	@Override
	public void cancel() {
		//No behaviour defined.		
	}
}
