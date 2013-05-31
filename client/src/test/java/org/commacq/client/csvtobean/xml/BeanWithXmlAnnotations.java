package org.commacq.client.csvtobean.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class BeanWithXmlAnnotations {

	@XmlAttribute
	String id;
	
	@XmlAttribute
	String name;
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
}