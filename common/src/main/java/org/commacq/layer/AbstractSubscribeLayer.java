package org.commacq.layer;

import java.util.Collection;

import org.commacq.CsvLineCallback;
import org.commacq.CsvLineCallbackComposite;

//TODO how do we protect against updates going to the new subscriber before all the results
//have been fetched?
public abstract class AbstractSubscribeLayer implements SubscribeLayer {

	protected final CsvLineCallbackComposite composite = new CsvLineCallbackComposite();

    @Override
    public final void getAllCsvLinesAndSubscribe(CsvLineCallback callback) {
    	composite.addCallback(callback);
    	getAllCsvLines(callback);
    }
    
    @Override
    public final void getAllCsvLinesAndSubscribe(Collection<String> entityIds, CsvLineCallback callback) {
    	composite.addCallback(entityIds, callback);
    	getAllCsvLines(entityIds, callback);
    }
    
    @Override
    public final void getAllCsvLinesAndSubscribe(String entityId, CsvLineCallback callback) {
    	composite.addCallback(entityId, callback);
    	getAllCsvLines(entityId, callback);
    }
    
    @Override
    public final void subscribe(CsvLineCallback callback) {
    	composite.addCallback(callback);
    }
    
    @Override
    public final void subscribe(Collection<String> entityIds, CsvLineCallback callback) {
    	composite.addCallback(entityIds, callback);
    }
    
    @Override
    public final void subscribe(String entityId, CsvLineCallback callback) {
    	composite.addCallback(entityId, callback);
    }
    
    @Override
    public final void unsubscribe(CsvLineCallback callback) {
    	composite.removeCallback(callback);
    }

}
