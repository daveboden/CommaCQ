package org.commacq;

public interface CompositeIdEncoding {
	
	String[] parseCompositeIdComponents(String id);
	
	String createCompositeId(String... components);

}
