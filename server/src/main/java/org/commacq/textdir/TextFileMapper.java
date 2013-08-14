package org.commacq.textdir;

/**
 * Call mapTextFile(id, text) for each filename (id) and contents (text)
 */
public interface TextFileMapper<ROW> {

	ROW mapTextFile(String id, String text);
	
}
