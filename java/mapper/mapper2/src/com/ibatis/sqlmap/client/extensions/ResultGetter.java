/*
 *  Copyright 2004 Clinton Begin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ibatis.sqlmap.client.extensions;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

/**
 * Allows values to be retrieved from the underlying result set.
 * TypeHandlerCallback implementations use this interface to
 * get values that they can subsequently manipulate before
 * having them returned.  Each of these methods has a corresponding
 * method on the ResultSet (or CallableStatement) class, the only
 * difference being that there is no need to specify the column name
 * or index with these methods.
 * <p/>
 * <b>NOTE:</b> There is no need to implement this.  The implementation
 * will be passed into the TypeHandlerCallback automatically.
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
