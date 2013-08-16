package org.commacq;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.lang3.StringEscapeUtils;
import org.commacq.db.StringColumnValueConverter;

/**
 * So, why choose to write yet another implementation
 * of a CSV utility? By taking total control over the
 * behaviour of this aspect of the program, we can ensure
 * that the output is exactly as desired and that the
 * performance is optimised without any compromises to
 * fitting in with a more generic library. We can avoid
 * creating temporary objects like string arrays and
 * instead go straight to CSV output lines. Seen as what
 * this server does all day is turn resultsets into CSV
 * it seems like a reasonable investment.
 * 
 * NULL is represented by absolutely no value between the
 * commas; for example: a,b,,d
 * Blank space is regarded as special and must be quoted;
 * for example: a,b,"",d
 * 
 * Dates are yyyy-MM-dd and are not surrounded by quotes.
 * Timestamps are yyyy-MM-ddTHH:mm:ssZZ (with timezone specified)
 * and are not surrounded by quotes.
 * 
 * If you want different behaviour, change your select statement
 * to return a string with exactly the format you want. Use
 * your database features rather than falling back to the default
 * behaviour provided by this class. Or, if you really want,
 * override the behaviour in this class and plug in your own
 * instance of this CsvParser.
 * 
 * BigDecimals are always in non-scientific notation and
 * quoted to the precision that they arrive in (with trailing
 * zeros if required). If you want to change the precision,
 * do it in your database select statement. They are not
 * surrounded by quotes.
 */
@ThreadSafe
public class CsvMarshaller {
	
	public static final char COMMA = ',';
	public static final char QUOTE = '"';
	public static final String EMPTY_STRING = "\"\"";
	
	public static final StringColumnValueConverter stringColumnValueConverter = new StringColumnValueConverter();
	
	private ThreadLocal<StringBuilder> stringBuilder = new ThreadLocal<StringBuilder>() {
		protected StringBuilder initialValue() {
			return new StringBuilder(2048);
		};
		
		@Override
		public StringBuilder get() {
			StringBuilder returnMe = super.get();
			returnMe.setLength(0); //Blank out the string builder for its next use
			return returnMe;
		}
	};

	public static class CsvLine {
		private String id;
		private String csvLine;
		
		public CsvLine(String id, String csvLine) {
			this.id = id;
			this.csvLine = csvLine;
		}
		
		public String getId() {
			return id;
		}
		
		public String getCsvLine() {
			return csvLine;
		}

		@Override
		public String toString() {
			return "CsvLine [id=" + id + ", csvLine=" + csvLine + "]";
		}		
	}
	
	public CsvLine toCsvLine(ResultSet result) throws SQLException {
		StringBuilder builder = stringBuilder.get();
		
        ResultSetMetaData metaData = result.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        if(columnCount <= 0) {
        	throw new SQLException("No columns to consider");
        }
        
        appendEscapedCsvEntry(builder, getColumnValue(result, metaData.getColumnType(1), 1));
        //The id is the first value to be added to the StringBuilder.
        //At this point, the builder just contains the pure id value;
        //so, let's capture it now.
        String id = builder.toString();
        	
        for (int i = 2; i <= metaData.getColumnCount(); i++) {
			builder.append(COMMA);
			appendEscapedCsvEntry(builder, getColumnValue(result, metaData.getColumnType(i), i));
        }
		
		return new CsvLine(id, builder.toString());
	}
	
	/**
	 * Column labels not column names.
	 * We want to take account of the "as" clause
	 */
	public String getColumnLabelsAsCsvLine(ResultSet result) throws SQLException {
		ResultSetMetaData metaData = result.getMetaData();
		int columnCount = metaData.getColumnCount();
		StringBuilder builder = stringBuilder.get();
		if(columnCount > 0) {
			appendEscapedCsvEntry(builder, metaData.getColumnLabel(1));
		}
		for(int i = 2; i <= columnCount; i++) {
			builder.append(COMMA);
			appendEscapedCsvEntry(builder, metaData.getColumnLabel(i));			
		}
		return builder.toString();
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
	
	protected String getColumnValue(ResultSet rs, int colType, int colIndex)
    		throws SQLException {

		return stringColumnValueConverter.getColumnValue(rs, colType, colIndex);

    }
	
}