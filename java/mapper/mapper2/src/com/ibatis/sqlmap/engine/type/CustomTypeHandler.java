package com.ibatis.sqlmap.engine.type;

import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;
import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.CallableStatement;

/**
 * User: Clinton
 * Date: 1-Aug-2004
 * Time: 7:38:39 AM
 */
public class CustomTypeHandler extends BaseTypeHandler implements TypeHandler {

  public TypeHandlerCallback callback;

  public CustomTypeHandler(TypeHandlerCallback callback) {
    this.callback = callback;
  }

  public void setParameter(PreparedStatement ps, int i, Object parameter, String jdbcType)
      throws SQLException {
    ParameterSetter setter = new ParameterSetterImpl(ps, i);
    callback.setParameter(setter, parameter);
  }

  public Object getResult(ResultSet rs, String columnName)
      throws SQLException {
    ResultGetter getter = new ResultGetterImpl(rs, columnName);
    return callback.getResult(getter);
  }

  public Object getResult(ResultSet rs, int columnIndex)
      throws SQLException {
    ResultGetter getter = new ResultGetterImpl(rs, columnIndex);
    return callback.getResult(getter);
  }

  public Object getResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    ResultGetter getter = new ResultGetterImpl(new CallableStatementResultSet(cs), columnIndex);
    return callback.getResult(getter);
  }

  public Object valueOf(String s) {
    return callback.valueOf(s);
  }

}
