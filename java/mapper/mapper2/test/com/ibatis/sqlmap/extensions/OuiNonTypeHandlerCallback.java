package com.ibatis.sqlmap.extensions;

import com.ibatis.sqlmap.client.extensions.*;
import com.ibatis.sqlmap.client.SqlMapException;

import java.sql.SQLException;

/**
 * User: Clinton
 * Date: 1-Aug-2004
 * Time: 5:59:46 PM
 */
public class OuiNonTypeHandlerCallback implements TypeHandlerCallback {

  public Object getResult(ResultGetter getter) throws SQLException {
    String s = getter.getString();
    if ("Oui".equalsIgnoreCase(s)) {
      return new Boolean (true);
    } else if ("Non".equalsIgnoreCase(s)) {
      return new Boolean (false);
    } else {
      throw new SQLException ("Unexpected value " + s + " found where 'Oue' or 'Non' was expected.");
    }
  }

  public void setParameter(ParameterSetter setter, Object parameter) throws SQLException {
    boolean b = ((Boolean)parameter).booleanValue();
    if (b) {
      setter.setString("Oui");
    } else {
      setter.setString("Non");
    }
  }

  public Object valueOf(String s) {
    if ("Oui".equalsIgnoreCase(s)) {
      return new Boolean (true);
    } else if ("Non".equalsIgnoreCase(s)) {
      return new Boolean (false);
    } else {
      throw new SqlMapException ("Unexpected value " + s + " found where 'Oue' or 'Non' was expected.");
    }
  }

}
