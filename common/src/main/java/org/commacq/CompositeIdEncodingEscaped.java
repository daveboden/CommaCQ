package org.commacq;

import org.apache.commons.lang3.StringUtils;

public class CompositeIdEncodingEscaped implements CompositeIdEncoding {

	public static final String SEPARATOR = "/";
	
	@Override
	public String[] parseCompositeIdComponents(String id) {

		String[] components = id.split("(?<!\\\\)\\/");
		
		for(int i = 0; i < components.length; i++) {
			components[i] = components[i]
					.replace("\\" + SEPARATOR, SEPARATOR)
					.replace("\\\\", "\\"); 
		}
		
		return components;
	}

	@Override
	public String createCompositeId(String... components) {
		boolean escapingRequired = false;
		for(String component : components) {
			if(component.contains(SEPARATOR)) {
				escapingRequired = true;
				break;
			}
		}
		
		if(escapingRequired) {
			String[] componentsEscaped = new String[components.length];
			int index = 0;
			for(String component : components) {
				componentsEscaped[index++] = component
						.replace("\\", "\\\\")
						.replace(SEPARATOR, "\\" + SEPARATOR);
			}
			
			components = componentsEscaped;
		}
		
		return StringUtils.join(components, SEPARATOR);
	}

}
