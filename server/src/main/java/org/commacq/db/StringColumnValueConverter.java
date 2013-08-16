package org.commacq.db;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class StringColumnValueConverter {
	
	private ThreadLocal<SimpleDateFormat> yyyy_MM_dd = new ThreadLocal<SimpleDateFormat>() {
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd");
		};
	};
	
	private ThreadLocal<SimpleDateFormat> yyyy_MM_dd_T_TimeWithZone = new ThreadLocal<SimpleDateFormat>() {
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		};
	};
	
	private ThreadLocal<DecimalFormat> decimalUpTo20Places = new ThreadLocal<DecimalFormat>() {
		protected DecimalFormat initialValue() {
			return new DecimalFormat("0.####################");
		};
	};
	
	/**
	 *TODO Potential optimisation; if we're returning a set value (e.g. true, false)
	 *     then there's no need to see whether the string needs escaping.
	 *     Perhaps escape as part of this method rather than doing it later?
	 */
	public String getColumnValue(ResultSet rs, int colType, int colIndex)
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
			case Types.DOUBLE:
			case Types.FLOAT:
				return handleDouble(rs.getDouble(colIndex));
			case Types.DECIMAL:
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
	
	protected String handleDouble(double d) {
		return decimalUpTo20Places.get().format(d);
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
            value =  yyyy_MM_dd.get().format(date);
        }
        return value;
    }

    private String handleTime(Time time) {
        return time == null ? null : time.toString();
    }
    
    private String handleTimestamp(Timestamp timestamp) {
        return timestamp == null ? null : yyyy_MM_dd_T_TimeWithZone.get().format(timestamp);
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