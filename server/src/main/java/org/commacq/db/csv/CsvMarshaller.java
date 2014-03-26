package org.commacq.db.csv;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.lang3.StringEscapeUtils;
import org.commacq.CompositeIdEncoding;
import org.commacq.CompositeIdEncodingEscaped;
import org.commacq.CsvLine;
import org.commacq.db.EntityConfig;
import org.commacq.db.RowExtractor;
import org.commacq.db.RowFactory;

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
	
	private final CompositeIdEncoding compositeIdEncoding = new CompositeIdEncodingEscaped();
	private final RowExtractor rowExtractor = new RowExtractor(compositeIdEncoding);
	
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
	
	public CsvLine toCsvLine(ResultSet result, EntityConfig entityConfig) throws SQLException {
		RowFactory<CsvLine> rowFactory = new RowFactoryCsv(stringBuilder.get());
		
		return rowExtractor.extractRow(entityConfig, result, rowFactory);
	}
	
	/**
	 * Column labels not column names.
	 * We want to take account of the "as" clause
	 */
	public String getColumnLabelsAsCsvLine(ResultSetMetaData metaData, Collection<String> groups) throws SQLException {
		int columnCount = metaData.getColumnCount();
		SortedSet<String> copyOfGroups = new TreeSet<String>(groups);
		StringBuilder builder = stringBuilder.get();
		if(columnCount > 0) {
			String columnLabel = metaData.getColumnLabel(1);
			appendEscapedCsvEntry(builder, columnLabel);
			copyOfGroups.remove(columnLabel);
		}
		for(int i = 2; i <= columnCount; i++) {
			builder.append(COMMA);
			String columnLabel = metaData.getColumnLabel(i);
			appendEscapedCsvEntry(builder, columnLabel);			
			copyOfGroups.remove(columnLabel);
		}
		if(!copyOfGroups.isEmpty()) {
			throw new SQLException("Groups specified that are not contained as columns in the query results: " + copyOfGroups);
		}
		return builder.toString();
	}
	
	
	public static final String EMPTY_STRING = "\"\"";
	
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