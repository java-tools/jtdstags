// jTDS JDBC Driver for Microsoft SQL Server and Sybase
// Copyright (C) 2004 The jTDS Project
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
package net.sourceforge.jtds.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.MalformedURLException;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

/**
 * jTDS implementation of the java.sql.CallableStatement interface.
 *<p>
 * Implementation note:
 * <ol>
 * <li>This class is a simple subclass of PreparedStatement and mainly adds support for
 * setting parameters.
 * <li>The class supports named parameters in a similar way to the
 * patch supplied by Tommy Sandstrom to the original jTDS code.
 * </ol>
 *
 * @author Mike Hutchinson
 * @version $Id: JtdsCallableStatement.java,v 1.5 2004-08-24 17:45:02 bheineman Exp $
 */
public class JtdsCallableStatement extends JtdsPreparedStatement implements CallableStatement {
    /**
     * Construct a CallableStatement object.
     *
     * @param connection The connection owning this statement.
     * @param sql The SQL statement specifying the procedure to call.
     * @param resultSetType The result set type eg FORWARD_ONLY.
     * @param concurrency   The result set concurrency eg READ_ONLY.
     * @throws SQLException
     */
    JtdsCallableStatement(ConnectionJDBC2 connection, String sql, int resultSetType, int concurrency)
        throws SQLException {
        super(connection, sql, resultSetType, concurrency, false);
    }

    /**
     * Find a parameter by name.
     * 
     * @param name The name of the parameter to locate.
     * @param set True if function is called from a set / register method.
     * @return The parameter index as an <code>int</code>.
     * @throws SQLException
     */
    int findParameter(String name, boolean set)
        throws SQLException {
        for (int i = 0; i < parameters.length; i++){
            if (parameters[i].name != null && parameters[i].name.equalsIgnoreCase(name))
                return i + 1;
        }

        if (set && !name.equalsIgnoreCase("@return_status")) {
            for (int i = 0; i < parameters.length; i++){
                if (parameters[i].name == null) {
                    parameters[i].name = name;

                    return i + 1;
                }
            }
        }

        throw new SQLException(Messages.get("error.callable.noparam", name), "07000");
    }

    /**
     * Check that this statement is still open.
     * 
     * @throws SQLException if statement closed.
     */
    protected void checkOpen() throws SQLException {
        if (closed) {
            throw new SQLException(
                    Messages.get("error.generic.closed", "CallableStatement"), "HY010");
        }
    }

// ---------- java.sql.CallableStatement methods follow ----------

    public boolean wasNull() throws SQLException {
        checkOpen();

        return paramWasNull;
    }

    public byte getByte(int parameterIndex) throws SQLException {
        ParamInfo pi = getParameter(parameterIndex);

        return ((Integer) Support.convert(this, pi.value, java.sql.Types.TINYINT, null)).byteValue();
    }

    public double getDouble(int parameterIndex) throws SQLException {
        ParamInfo pi = getParameter(parameterIndex);

        return ((Double) Support.convert(this, pi.value, java.sql.Types.DOUBLE, null)).doubleValue();
    }

    public float getFloat(int parameterIndex) throws SQLException {
        ParamInfo pi = getParameter(parameterIndex);

        return ((Double) Support.convert(this, pi.value, java.sql.Types.FLOAT, null)).floatValue();
    }

    public int getInt(int parameterIndex) throws SQLException {
        ParamInfo pi = getParameter(parameterIndex);

        return ((Integer) Support.convert(this, pi.value, java.sql.Types.INTEGER, null)).intValue();
    }

    public long getLong(int parameterIndex) throws SQLException {
        ParamInfo pi = getParameter(parameterIndex);

        return ((Long) Support.convert(this, pi.value, java.sql.Types.BIGINT, null)).longValue();
    }

    public short getShort(int parameterIndex) throws SQLException {
        ParamInfo pi = getParameter(parameterIndex);

        return ((Integer) Support.convert(this, pi.value, java.sql.Types.SMALLINT, null)).shortValue();
    }

    public boolean getBoolean(int parameterIndex) throws SQLException {
        ParamInfo pi = getParameter(parameterIndex);

        return ((Boolean) Support.convert(this, pi.value, BOOLEAN, null)).booleanValue();
    }

    public byte[] getBytes(int parameterIndex) throws SQLException {
        ParamInfo pi = getParameter(parameterIndex);

        return ((byte[]) Support.convert(this, pi.value, java.sql.Types.VARBINARY, connection.getCharSet()));
    }

    public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException {
        if (sqlType == java.sql.Types.DECIMAL
            || sqlType == java.sql.Types.NUMERIC) {
            registerOutParameter(parameterIndex, sqlType, -1);
        } else {
            registerOutParameter(parameterIndex, sqlType, 0);
        }
    }

    public void registerOutParameter(int parameterIndex, int sqlType, int scale)
        throws SQLException {
        ParamInfo pi = getParameter(parameterIndex);

        pi.isOutput = true;

        if (sqlType == java.sql.Types.CLOB) {
            pi.jdbcType = java.sql.Types.LONGVARCHAR;
        } else if (sqlType == java.sql.Types.BLOB) {
            pi.jdbcType = java.sql.Types.LONGVARBINARY;
        } else {
            pi.jdbcType = sqlType;
        }

        pi.scale = scale;
    }

    public Object getObject(int parameterIndex) throws SQLException {
        ParamInfo pi = getParameter(parameterIndex);

        return pi.value;
    }

    public String getString(int parameterIndex) throws SQLException {
        ParamInfo pi = getParameter(parameterIndex);

        return (String) Support.convert(this, pi.value, java.sql.Types.VARCHAR, connection.getCharSet());
    }

    public void registerOutParameter(int parameterIndex, int sqlType, String typeName)
        throws SQLException {
        notImplemented("CallableStatement.registerOutParameter(int, int, String");
    }

    public byte getByte(String parameterName) throws SQLException {
        return getByte(findParameter(parameterName, false));
    }

    public double getDouble(String parameterName) throws SQLException {
        return getDouble(findParameter(parameterName, false));
    }

    public float getFloat(String parameterName) throws SQLException {
        return getFloat(findParameter(parameterName, false));
    }

    public int getInt(String parameterName) throws SQLException {
        return getInt(findParameter(parameterName, false));
    }

    public long getLong(String parameterName) throws SQLException {
        return getLong(findParameter(parameterName, false));
    }

    public short getShort(String parameterName) throws SQLException {
        return getShort(findParameter(parameterName, false));
    }

    public boolean getBoolean(String parameterName) throws SQLException {
        return getBoolean(findParameter(parameterName, false));
    }

    public byte[] getBytes(String parameterName) throws SQLException {
        return getBytes(findParameter(parameterName, false));
    }

    public void setByte(String parameterName, byte x) throws SQLException {
        setByte(findParameter(parameterName, false), x);
    }

    public void setDouble(String parameterName, double x) throws SQLException {
        setDouble(findParameter(parameterName, false), x);
    }

    public void setFloat(String parameterName, float x) throws SQLException {
        setFloat(findParameter(parameterName, false), x);
    }

    public void registerOutParameter(String parameterName, int sqlType)
        throws SQLException {
        registerOutParameter(findParameter(parameterName, true), sqlType);
    }

    public void setInt(String parameterName, int x) throws SQLException {
        setInt(findParameter(parameterName, true), x);
    }

    public void setNull(String parameterName, int sqlType) throws SQLException {
        setNull(findParameter(parameterName, true), sqlType);
    }

    public void registerOutParameter(String parameterName, int sqlType, int scale)
        throws SQLException {
        registerOutParameter(findParameter(parameterName, true), sqlType, scale);
    }

    public void setLong(String parameterName, long x) throws SQLException {
        setLong(findParameter(parameterName, true), x);
    }

    public void setShort(String parameterName, short x) throws SQLException {
        setShort(findParameter(parameterName, true), x);
    }

    public void setBoolean(String parameterName, boolean x) throws SQLException {
        setBoolean(findParameter(parameterName, true), x);
    }

    public void setBytes(String parameterName, byte[] x) throws SQLException {
        setBytes(findParameter(parameterName, true), x);
    }

    public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
        ParamInfo pi = getParameter(parameterIndex);

        return (BigDecimal) Support.convert(this, pi.value, java.sql.Types.DECIMAL, null);
    }

    public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
        ParamInfo pi = getParameter(parameterIndex);
        BigDecimal bd = (BigDecimal) Support.convert(this, pi.value, java.sql.Types.DECIMAL, null);

        return bd.setScale(scale);
    }

    public URL getURL(int parameterIndex) throws SQLException {
        ParamInfo pi = getParameter(parameterIndex);
        String url = (String) Support.convert(this, pi.value, java.sql.Types.VARCHAR, connection.getCharSet());

        try {
            return new java.net.URL(url);
        } catch (MalformedURLException e) {
            throw new SQLException(Messages.get("error.resultset.badurl", url), "22000");
        }
    }

    public Array getArray(int parameterIndex) throws SQLException {
        notImplemented("CallableStatement.getArray");
        return null;
    }

    public Blob getBlob(int parameterIndex) throws SQLException {
        byte[] value = getBytes(parameterIndex);

        if (value == null) {
            return null;
        }

        return new BlobImpl(connection, value);
    }

    public Clob getClob(int parameterIndex) throws SQLException {
        String value = getString(parameterIndex);

        if (value == null) {
            return null;
        }

        return new ClobImpl(connection, value);
    }

    public Date getDate(int parameterIndex) throws SQLException {
        ParamInfo pi = getParameter(parameterIndex);

        return (java.sql.Date) Support.convert(this, pi.value, java.sql.Types.DATE, null);
    }

    public Ref getRef(int parameterIndex) throws SQLException {
        notImplemented("CallableStatement.getRef");
        return null;
    }

    public Time getTime(int parameterIndex) throws SQLException {
        ParamInfo pi = getParameter(parameterIndex);

        return (Time) Support.convert(this, pi.value, java.sql.Types.TIME, null);
    }

    public Timestamp getTimestamp(int parameterIndex) throws SQLException {
        ParamInfo pi = getParameter(parameterIndex);

        return (Timestamp) Support.convert(this, pi.value, java.sql.Types.TIMESTAMP, null);
    }

    public void setAsciiStream(String parameterName, InputStream x, int length)
        throws SQLException {
        setAsciiStream(findParameter(parameterName, true), x, length);
    }

    public void setBinaryStream(String parameterName, InputStream x, int length)
        throws SQLException {
        setBinaryStream(findParameter(parameterName, true), x, length);
    }

    public void setCharacterStream(String parameterName, Reader reader, int length)
        throws SQLException {
        setCharacterStream(findParameter(parameterName, true), reader, length);
    }

    public Object getObject(String parameterName) throws SQLException {
        return getObject(findParameter(parameterName, false));
    }

    public void setObject(String parameterName, Object x) throws SQLException {
        setObject(findParameter(parameterName, true), x);
    }

    public void setObject(String parameterName, Object x, int targetSqlType)
        throws SQLException {
        setObject(findParameter(parameterName, true), x, targetSqlType);
    }

    public void setObject(String parameterName, Object x, int targetSqlType, int scale)
        throws SQLException {
        setObject(findParameter(parameterName, true), x, targetSqlType, scale);
    }

    public Object getObject(int parameterIndex, Map map) throws SQLException {
        notImplemented("CallableStatement.getObject(int, Map)");
        return null;
    }

    public String getString(String parameterName) throws SQLException {
        return getString(findParameter(parameterName, false));
    }

    public void registerOutParameter(String parameterName, int sqlType, String typeName)
        throws SQLException {
        notImplemented("CallableStatement.registerOutParameter(String, int, String");
    }

    public void setNull(String parameterName, int sqlType, String typeName)
        throws SQLException {
        notImplemented("CallableStatement.setNull(String, int, String");
    }

    public void setString(String parameterName, String x) throws SQLException {
        setString(findParameter(parameterName, true), x);
    }

    public BigDecimal getBigDecimal(String parameterName) throws SQLException {
        return getBigDecimal(findParameter(parameterName, false));
    }

    public void setBigDecimal(String parameterName, BigDecimal x)
        throws SQLException {
        setBigDecimal(findParameter(parameterName, true), x);
    }

    public URL getURL(String parameterName) throws SQLException {
        return getURL(findParameter(parameterName, false));
    }

    public void setURL(String parameterName, URL x) throws SQLException {
        setObject(findParameter(parameterName, true), x);
    }

    public Array getArray(String parameterName) throws SQLException {
        return getArray(findParameter(parameterName, false));
    }

    public Blob getBlob(String parameterName) throws SQLException {
        return getBlob(findParameter(parameterName, false));
    }

    public Clob getClob(String parameterName) throws SQLException {
        return getClob(findParameter(parameterName, false));
    }

    public Date getDate(String parameterName) throws SQLException {
        return getDate(findParameter(parameterName, false));
    }

    public void setDate(String parameterName, Date x) throws SQLException {
        setDate(findParameter(parameterName, true), x);
    }

    public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
        java.sql.Date date = getDate(parameterIndex);

        if (date != null && cal != null) {
            TimeZone timeZone = TimeZone.getDefault();
            long newTime = date.getTime();

            newTime -= timeZone.getRawOffset();
            newTime += cal.getTimeZone().getRawOffset();
            date = new java.sql.Date(newTime);
        }

        return date;
    }

    public Ref getRef(String parameterName) throws SQLException {
        return getRef(findParameter(parameterName, false));
    }

    public Time getTime(String parameterName) throws SQLException {
        return getTime(findParameter(parameterName, false));
    }

    public void setTime(String parameterName, Time x) throws SQLException {
        setTime(findParameter(parameterName, true), x);
    }

    public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
        java.sql.Time time = getTime(parameterIndex);

        if (time != null && cal != null) {
            TimeZone timeZone = TimeZone.getDefault();
            long newTime = time.getTime();

            newTime -= timeZone.getRawOffset();
            newTime += cal.getTimeZone().getRawOffset();
            time = new java.sql.Time(newTime);
        }

        return time;
    }

    public Timestamp getTimestamp(String parameterName) throws SQLException {
        return getTimestamp(findParameter(parameterName, false));
    }

    public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
        setTimestamp(findParameter(parameterName, true), x);
    }

    public Timestamp getTimestamp(int parameterIndex, Calendar cal)
        throws SQLException {
        Timestamp timestamp = getTimestamp(parameterIndex);

        if (timestamp != null && cal != null) {
            TimeZone timeZone = TimeZone.getDefault();
            long newTime = timestamp.getTime();

            newTime -= timeZone.getRawOffset();
            newTime += cal.getTimeZone().getRawOffset();
            timestamp = new Timestamp(newTime);
        }

        return timestamp;
    }

    public Object getObject(String parameterName, Map map) throws SQLException {
         return getObject(findParameter(parameterName, false), map);
    }

    public Date getDate(String parameterName, Calendar cal) throws SQLException {
        return getDate(findParameter(parameterName, false), cal);
    }

    public Time getTime(String parameterName, Calendar cal) throws SQLException {
        return getTime(findParameter(parameterName, false), cal);
    }

    public Timestamp getTimestamp(String parameterName, Calendar cal)
        throws SQLException {
        return getTimestamp(findParameter(parameterName, false), cal);
    }

    public void setDate(String parameterName, Date x, Calendar cal)
        throws SQLException {
        setDate(findParameter(parameterName, true), x, cal);
    }

    public void setTime(String parameterName, Time x, Calendar cal)
        throws SQLException {
        setTime(findParameter(parameterName, true), x, cal);
    }

    public void setTimestamp(String parameterName, Timestamp x, Calendar cal)
        throws SQLException {
        setTimestamp(findParameter(parameterName, true), x, cal);
    }
}
