package org.commacq;


/**
 * If a child layer requests data using the getter interface of a parent layer and the child
 * has a problem processing one of the callbacks then the child will throw an exception from
 * the callback method. The parent layer, which is delivering data via the callback mechanism,
 * should respond by sending no more data. It should not log the error; it's the child's
 * responsibility to log the error, explaining the cause of the problem, before throwing the exception.
 * 
 * If a parent layer is delivering data to a child layer as part of a subscription, the parent
 * should respond to an exception from the child by removing the subscription callback.
 * It's the child's responsibility to reinitialise and resubscribe when it has recovered from the error.
 */
public class CsvUpdateBlockException extends Exception {

	private static final long serialVersionUID = 1L;

	public CsvUpdateBlockException() {
		super();
	}
	
    public CsvUpdateBlockException(String message) {
        super(message);
    }

    public CsvUpdateBlockException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public CsvUpdateBlockException(Throwable cause) {
        super(cause);
    }
}
