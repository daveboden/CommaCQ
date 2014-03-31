package org.commacq;

import org.apache.commons.lang3.StringUtils;

public class CompositeIdEncodingSimple implements CompositeIdEncoding {

	public static final String SEPARATOR = "/";
	
	@Override
	public String[] parseCompositeIdComponents(String id) {
		String[] split = id.split(SEPARATOR, -1);
		for(int i = 0; i < split.length; i++) {
			if(split[i].equals("")) {
				split[i] = null;
			}
		}
		return split;
	}

	@Override
	public String createCompositeId(String... components) {
		return StringUtils.join(components, SEPARATOR);
	}
	
}
