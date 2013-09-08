package org.commacq;

/**
 * Implements the bulk and bulk-group methods of CsvLineCallback with
 * no behaviour. The implementations are marked as final so that a
 * developer isn't tempted to use this class then override one of the
 * methods and add behaviour. That would be confusing. A developer should
 * implement CsvLineCallback directly if bulk behaviour is required.
 */
public abstract class CsvLineCallbackAbstractSimple implements CsvLineCallback {

	@Override
	public final void startBulkUpdate(String columnNamesCsv) {
		//No behaviour defined.
	}
	
	@Override
	public void startUpdateBlock(String columnNamesCsv) {
		//No behaviour defined.		
	}
	
	@Override
	public final void finishUpdateBlock() {
		//No behaviour defined.
	}
	
	@Override
	public final void startBulkUpdateForGroup(String group, String idWithinGroup) {
		//No behaviour defined.
	}
}
