package org.commacq.db.csv;

import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.apache.commons.lang3.StringEscapeUtils;
import org.commacq.CsvLine;
import org.commacq.db.RowFactory;

@RequiredArgsConstructor
public class RowFactoryCsv implements RowFactory<CsvLine> {

	public static final char COMMA = ',';
	public static final String EMPTY_STRING = "\"\"";
	
	private String id;
	private final StringBuilder builder;
	
	@Getter
	@Setter
	private Map<String, String> groupValues;
	
	@Override
	public void addValue(String columnName, String columnValue) {
		builder.append(COMMA);
		appendEscapedCsvEntry(builder, columnValue);
	}

	@Override
	public CsvLine getObject() {
		return new CsvLine(id, builder.toString(), groupValues);
	}
	
	@Override
	public void setId(String idValue) {
		id = idValue;
		builder.insert(0, StringEscapeUtils.escapeCsv(idValue));
	}
	
	/**
	 * Adds the CSV entry, escaping where required. Does not add a comma.
	 * @return the String that was appended
	 */
	protected void appendEscapedCsvEntry(final StringBuilder builder, final String value) {
		if(value == null) {
			//nulls end up as separators with nothing inbetween ,,
			return;
		}
		
		if(value.isEmpty()) {
			//Empty string is treated as a special case to differentiate
			//it from null. It's quoted.
			builder.append(EMPTY_STRING);
			return;
		}
		
		String escapedString = StringEscapeUtils.escapeCsv(value);
		builder.append(escapedString);		
	}
	
}
