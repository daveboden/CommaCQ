package org.commacq;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;

import org.apache.commons.lang3.StringEscapeUtils;

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
public class CsvParser {
	
	public static final char COMMA = ',';
	public static final char QUOTE = '"';
	public static final String EMPTY_STRING = "\"\"";


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
		StringBuilder builder = new StringBuilder();
		
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
		//TODO consider sharing StringBuilder and making this
		//class non-threadsafe.
		StringBuilder builder = new StringBuilder();
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
	
	/**
	 *TODO Potential optimisation; if we're returning a set value (e.g. true, false)
	 *     then there's no need to see whether the string needs escaping.
	 *     Perhaps escape as part of this method rather than doing it later?
	 */
	protected String getColumnValue(ResultSet rs, int colType, int colIndex)
    		throws SQLException {

		switch (colType)
		{				
			case Types.BOOLEAN:
				boolean b = rs.getBoolean(colIndex);
				return Boolean.valueOf(b).toString();
				
			case Types.NCLOB:
			case Types.CLOB:
				Clob c = rs.getClob(colIndex);
				if (c != null) {
					try {
						return read(c);
					} catch(IOException ex) {
						throw new SQLException("Error reading CLOB", ex);
					}
				} else {
					return null;
				}
				
			case Types.BIGINT:
				return handleLong(rs, colIndex);
			case Types.BIT:
				return handleBoolean(rs.getBoolean(colIndex));
			case Types.DECIMAL:
			case Types.DOUBLE:
			case Types.FLOAT:
				//TODO Handle floating point types properly
			case Types.REAL:
			case Types.NUMERIC:
				return handleBigDecimal(rs.getBigDecimal(colIndex));
			case Types.INTEGER:
			case Types.TINYINT:
			case Types.SMALLINT:
                return handleInteger(rs, colIndex);
			case Types.DATE:
				return handleDate(rs, colIndex);
			case Types.TIME:
				return handleTime(rs.getTime(colIndex));
			case Types.TIMESTAMP:
				return handleTimestamp(rs.getTimestamp(colIndex));
			case Types.NVARCHAR: // todo : use rs.getNString
			case Types.NCHAR: // todo : use rs.getNString
			case Types.LONGNVARCHAR: // todo : use rs.getNString
			case Types.LONGVARCHAR:
			case Types.VARCHAR:
			case Types.CHAR:
				return rs.getString(colIndex);
			default:
				throw new SQLException("Unsupported type: " + colType);
				
		}

    }
    
	
	protected String handleBoolean(boolean b) {
		return Boolean.toString(b);
	}
	
	protected String handleBigDecimal(BigDecimal decimal) {
        return decimal == null ? null : decimal.toString();
    }

	protected String handleLong(ResultSet rs, int columnIndex) throws SQLException {
        long lv = rs.getLong(columnIndex);
        return rs.wasNull() ? null : Long.toString(lv);
    }

	protected String handleInteger(ResultSet rs, int columnIndex) throws SQLException {
        int i = rs.getInt(columnIndex);
        return rs.wasNull() ? null : Integer.toString(i);
    }

	protected String handleDate(ResultSet rs, int columnIndex) throws SQLException {
        java.sql.Date date = rs.getDate(columnIndex);
        String value = null;
        if (date != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            value =  dateFormat.format(date);
        }
        return value;
    }

    private String handleTime(Time time) {
        return time == null ? null : time.toString();
    }
    
    private String handleTimestamp(Timestamp timestamp) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        return timestamp == null ? null : timeFormat.format(timestamp);
    }
    
    private static final int CLOBBUFFERSIZE = 2048;
    private static String read(Clob c) throws SQLException, IOException
	{
		StringBuilder sb = new StringBuilder( (int) c.length());
		Reader r = c.getCharacterStream();
		char[] cbuf = new char[CLOBBUFFERSIZE];
		int n;
		while ((n = r.read(cbuf, 0, cbuf.length)) != -1) {
				sb.append(cbuf, 0, n);
		}
		return sb.toString();
	}
	
}
