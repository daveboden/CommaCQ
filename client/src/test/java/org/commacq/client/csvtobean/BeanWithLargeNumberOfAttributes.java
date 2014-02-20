package org.commacq.client.csvtobean;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class BeanWithLargeNumberOfAttributes {

	@XmlAttribute
	String id;
	
	@XmlAttribute
	String name0;
	@XmlAttribute
	String name1;
	@XmlAttribute
	String name2;
	@XmlAttribute
	String name3;
	@XmlAttribute
	String name4;
	@XmlAttribute
	String name5;
	@XmlAttribute
	String name6;
	@XmlAttribute
	String name7;
	@XmlAttribute
	String name8;
	@XmlAttribute
	String name9;
	@XmlAttribute
	String name10;
	@XmlAttribute
	String name11;
	@XmlAttribute
	String name12;
	@XmlAttribute
	String name13;
	@XmlAttribute
	String name14;
	@XmlAttribute
	String name15;
	@XmlAttribute
	String name16;
	@XmlAttribute
	String name17;
	@XmlAttribute
	String name18;
	@XmlAttribute
	String name19;
	
	//For jaxb construction strategy
	public BeanWithLargeNumberOfAttributes() {
	}
	
	//For spring construction strategy
	public BeanWithLargeNumberOfAttributes(String id, String name0,
			String name1, String name2, String name3, String name4,
			String name5, String name6, String name7, String name8,
			String name9, String name10, String name11, String name12,
			String name13, String name14, String name15, String name16,
			String name17, String name18, String name19) {
		this.id = id;
		this.name0 = name0;
		this.name1 = name1;
		this.name2 = name2;
		this.name3 = name3;
		this.name4 = name4;
		this.name5 = name5;
		this.name6 = name6;
		this.name7 = name7;
		this.name8 = name8;
		this.name9 = name9;
		this.name10 = name10;
		this.name11 = name11;
		this.name12 = name12;
		this.name13 = name13;
		this.name14 = name14;
		this.name15 = name15;
		this.name16 = name16;
		this.name17 = name17;
		this.name18 = name18;
		this.name19 = name19;
	}

	public String getId() {
		return id;
	}
	
	public String getName0() {
		return name0;
	}

	public String getName1() {
		return name1;
	}

	public String getName2() {
		return name2;
	}

	public String getName3() {
		return name3;
	}

	public String getName4() {
		return name4;
	}

	public String getName5() {
		return name5;
	}

	public String getName6() {
		return name6;
	}

	public String getName7() {
		return name7;
	}

	public String getName8() {
		return name8;
	}

	public String getName9() {
		return name9;
	}

	public String getName10() {
		return name10;
	}

	public String getName11() {
		return name11;
	}

	public String getName12() {
		return name12;
	}

	public String getName13() {
		return name13;
	}

	public String getName14() {
		return name14;
	}

	public String getName15() {
		return name15;
	}

	public String getName16() {
		return name16;
	}

	public String getName17() {
		return name17;
	}

	public String getName18() {
		return name18;
	}

	public String getName19() {
		return name19;
	}
	
}
