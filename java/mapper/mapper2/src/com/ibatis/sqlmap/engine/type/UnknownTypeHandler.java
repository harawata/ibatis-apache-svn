package com.ibatis.sqlmap.engine.type;

import java.sql.*;

/**
 * User: Clinton Begin
 * Date: Nov 23, 2003
 * Time: 10:39:10 PM
 */
public class UnknownTypeHandler extends BaseTypeHandler implements TypeHandler {

  private TypeHandlerFactory factory;

  public UnknownTypeHandler(TypeHandlerFactory factory) {
    this.factory = factory;
  }

  public void setParameter(PreparedStatement ps, int i, Object parameter, String jdbcType)
      throws SQLException {

    TypeHandler handler = factory.getTypeHandler(parameter.getClass(), jdbcType);
    handler.setParameter(ps, i, parameter, jdbcType);

  }

  public Object getResult(ResultSet rs, String columnName)
      throws SQLException {
    Object object = rs.getObject(columnName);
    if (rs.wasNull()) {
      return null;
    } else {
      return object;
    }
  }

  public Object getResult(ResultSet rs, int columnIndex)
      throws SQLException {
    Object object = rs.getObject(columnIndex);
    if (rs.wasNull()) {
      return null;
    } else {
      return object;
    }
  }

  public Object getResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    Object object = cs.getObject(columnIndex);
    if (cs.wasNull()) {
      return null;
    } else {
      return object;
    }
  }

  public Object valueOf(String s) {
    return s;
  }

  public boolean equals(Object object, String string) {
    if (object == null || string == null) {
      return object == string;
    } else {
      TypeHandler handler = factory.getTypeHandler(object.getClass());
      Object castedObject = handler.valueOf(string);
      return object.equals(castedObject);
    }
  }

}
