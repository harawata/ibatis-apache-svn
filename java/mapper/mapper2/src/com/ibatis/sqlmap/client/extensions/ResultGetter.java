package com.ibatis.sqlmap.client.extensions;

import java.sql.*;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Map;
import java.net.URL;

/**
 * User: Clinton
 * Date: 1-Aug-2004
 * Time: 7:40:54 AM
 */
public interface ResultGetter {

  public Array getArray() throws SQLException;

  public BigDecimal getBigDecimal() throws SQLException;

  public Blob getBlob() throws SQLException;

  public boolean getBoolean() throws SQLException;

  public byte getByte() throws SQLException;

  public byte[] getBytes() throws SQLException;

  public Clob getClob() throws SQLException;

  public Date getDate() throws SQLException;

  public Date getDate(Calendar cal) throws SQLException;

  public double getDouble() throws SQLException;

  public float getFloat() throws SQLException;

  public int getInt() throws SQLException;

  public long getLong() throws SQLException;

  public Object getObject() throws SQLException;

  public Object getObject(Map map) throws SQLException;

  public Ref getRef() throws SQLException;

  public short getShort() throws SQLException;

  public String getString() throws SQLException;

  public Time getTime() throws SQLException;

  public Time getTime(Calendar cal) throws SQLException;

  public Timestamp getTimestamp() throws SQLException;

  public Timestamp getTimestamp(Calendar cal) throws SQLException;

  public URL getURL() throws SQLException;

  public boolean wasNull() throws SQLException;
}
