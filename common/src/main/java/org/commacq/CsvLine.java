package org.commacq;

import java.util.Collections;
import java.util.Map;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Getter
public class CsvLine {
	private final String id;
	private final String csvLine;
	private final Map<String, String> groupValues;
	
	public CsvLine(String id, String csvLine, Map<String, String> groupValues) {
		this.id = id;
		this.csvLine = csvLine;
		this.groupValues = groupValues;
	}
		
	@SuppressWarnings("unchecked")
	public CsvLine(String id, String csvLine) {
		this(id, csvLine, Collections.EMPTY_MAP);
	}

}
