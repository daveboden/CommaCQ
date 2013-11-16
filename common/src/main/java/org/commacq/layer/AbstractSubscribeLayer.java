package org.commacq.layer;

import java.util.Collection;

import org.commacq.BlockCallback;
import org.commacq.CsvLineCallbackComposite;

//TODO how do we protect against updates going to the new subscriber before all the results
//have been fetched?
public abstract class AbstractSubscribeLayer implements SubscribeLayer {

	protected final CsvLineCallbackComposite composite = new CsvLineCallbackComposite();

    @Override
    public final void getAllCsvLinesAndSubscribe(BlockCallback callback) {
    	composite.addCallback(callback);
    	getAllCsvLines(callback);
    }
    
    @Override
    public final void getAllCsvLinesAndSubscribe(Collection<String> entityIds, BlockCallback callback) {
    	composite.addCallback(entityIds, callback);
    	getAllCsvLines(entityIds, callback);
    }
    
    @Override
    public final void getAllCsvLinesAndSubscribe(String entityId, BlockCallback callback) {
    	composite.addCallback(entityId, callback);
    	getAllCsvLines(entityId, callback);
    }
    
    @Override
    public final void subscribe(BlockCallback callback) {
    	composite.addCallback(callback);
    }
    
    @Override
    public final void subscribe(Collection<String> entityIds, BlockCallback callback) {
    	composite.addCallback(entityIds, callback);
    }
    
    @Override
    public final void subscribe(String entityId, BlockCallback callback) {
    	composite.addCallback(entityId, callback);
    }
    
    @Override
    public final void unsubscribe(BlockCallback callback) {
    	composite.removeCallback(callback);
    }

}
