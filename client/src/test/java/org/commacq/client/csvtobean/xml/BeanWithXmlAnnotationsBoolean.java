package org.commacq.client.csvtobean.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class BeanWithXmlAnnotationsBoolean {

	@XmlAttribute
	String id;
	
	@XmlAttribute
	boolean active;
	
	public String getId() {
		return id;
	}
	
	public boolean isActive() {
		return active;
	}
	
}