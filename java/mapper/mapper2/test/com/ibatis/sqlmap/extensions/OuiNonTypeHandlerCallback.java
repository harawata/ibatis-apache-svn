package com.ibatis.sqlmap.extensions;

import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;

import java.sql.SQLException;

public class OuiNonTypeHandlerCallback implements TypeHandlerCallback {

  public Object getResult(ResultGetter getter) throws SQLException {
    String s = getter.getString();
    if ("Oui".equalsIgnoreCase(s)) {
      return new Boolean(true);
    } else if ("Non".equalsIgnoreCase(s)) {
      return new Boolean(false);
    } else {
      throw new SQLException("Unexpected value " + s + " found where 'Oue' or 'Non' was expected.");
    }
  }

  public void setParameter(ParameterSetter setter, Object parameter) throws SQLException {
    boolean b = ((Boolean) parameter).booleanValue();
    if (b) {
      setter.setString("Oui");
    } else {
      setter.setString("Non");
    }
  }

  public Object valueOf(String s) {
    if ("Oui".equalsIgnoreCase(s)) {
      return new Boolean(true);
    } else if ("Non".equalsIgnoreCase(s)) {
      return new Boolean(false);
    } else {
      throw new SqlMapException("Unexpected value " + s + " found where 'Oue' or 'Non' was expected.");
    }
  }

}
