package org.commacq;

import org.apache.commons.lang3.StringUtils;

public class CompositeIdEncodingSimple implements CompositeIdEncoding {

	public static final String SEPARATOR = "/";
	
	@Override
	public String[] parseCompositeIdComponents(String id) {
		return id.split(SEPARATOR);
	}

	@Override
	public String createCompositeId(String... components) {
		return StringUtils.join(components, SEPARATOR);
	}
	
}
