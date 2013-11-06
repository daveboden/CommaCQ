package org.commacq;

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
