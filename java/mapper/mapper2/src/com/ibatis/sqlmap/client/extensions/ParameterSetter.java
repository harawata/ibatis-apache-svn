package com.ibatis.sqlmap.client.extensions;

import java.sql.*;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Calendar;
import java.net.URL;

/**
 * Allows parameters to be set on the underlying prepared statement.
 * TypeHandlerCallback implementations use this interface to
 * process values before they are set on the prepared statement.
 * Each of these methods has a corresponding method on the
 * PreparedStatement class, the only difference being
 * that there is no need to specify the parameter index with these
 * methods.
 * <p/>
 * <b>NOTE:</b> There is no need to implement this.  The implementation
 * will be passed into the TypeHandlerCallback automatically.
 */
public interface ParameterSetter {

  public void setArray(Array x) throws SQLException;

  public void setAsciiStream(InputStream x, int length) throws SQLException;

  public void setBigDecimal(BigDecimal x) throws SQLException;

  public void setBinaryStream(InputStream x, int length) throws SQLException;

  public void setBlob(Blob x) throws SQLException;

  public void setBoolean(boolean x) throws SQLException;

  public void setByte(byte x) throws SQLException;

  public void setBytes(byte x[]) throws SQLException;

  public void setCharacterStream(Reader reader, int length) throws SQLException;

  public void setClob(Clob x) throws SQLException;

  public void setDate(Date x) throws SQLException;

  public void setDate(Date x, Calendar cal) throws SQLException;

  public void setDouble(double x) throws SQLException;

  public void setFloat(float x) throws SQLException;

  public void setInt(int x) throws SQLException;

  public void setLong(long x) throws SQLException;

  public void setNull(int sqlType) throws SQLException;

  public void setNull(int sqlType, String typeName) throws SQLException;

  public void setObject(Object x) throws SQLException;

  public void setObject(Object x, int targetSqlType) throws SQLException;

  public void setObject(Object x, int targetSqlType, int scale) throws SQLException;

  public void setRef(Ref x) throws SQLException;

  public void setShort(short x) throws SQLException;

  public void setString(String x) throws SQLException;

  public void setTime(Time x) throws SQLException;

  public void setTime(Time x, Calendar cal) throws SQLException;

  public void setTimestamp(Timestamp x) throws SQLException;

  public void setTimestamp(Timestamp x, Calendar cal) throws SQLException;

  public void setURL(URL x) throws SQLException;

}
