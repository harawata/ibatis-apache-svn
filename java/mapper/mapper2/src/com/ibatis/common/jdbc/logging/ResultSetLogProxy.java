/**
 * User: Clinton Begin
 * Date: May 22, 2003
 * Time: 8:45:08 PM
 */
package com.ibatis.common.jdbc.logging;

import java.lang.reflect.*;
import java.sql.*;

import org.apache.commons.logging.*;
import com.ibatis.common.beans.ClassInfo;

public class ResultSetLogProxy extends BaseLogProxy implements InvocationHandler {

  private static final Log log = LogFactory.getLog(ResultSet.class);

  boolean first = true;
  private ResultSet rs;

  private ResultSetLogProxy(ResultSet rs) {
    super();
    this.rs = rs;
    if (log.isDebugEnabled()) {
      log.debug("{rset-" + id + "} ResultSet");
    }
  }

  public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
    try {
      Object o = method.invoke(rs, params);
      if (GET_METHODS.contains(method.getName())) {
        if (params[0] instanceof String) {
          setColumn(params[0], o);
  //        setColumn(params[0], rs.getObject((String) params[0]));
  //      } else {
  //        setColumn(params[0], rs.getObject(((Integer) params[0]).intValue()));
        }
      } else if ("next".equals(method.getName())) {
        String s = getValueString();
        if (!"[]".equals(s)) {
          if (first) {
            first = false;
            if (log.isDebugEnabled()) {
              log.debug("{rset-" + id + "} Header: " + getColumnString());
            }
          }
          if (log.isDebugEnabled()) {
            log.debug("{rset-" + id + "} Result: " + s);
          }
        }
        clearColumnInfo();
      }
      return o;
    } catch (Throwable t) {
      throw ClassInfo.unwrapThrowable(t);
    }
  }

  public static ResultSet newInstance(ResultSet rs) {
    InvocationHandler handler = new ResultSetLogProxy(rs);
    ClassLoader cl = ResultSet.class.getClassLoader();
    return (ResultSet) Proxy.newProxyInstance(cl, new Class[]{ResultSet.class}, handler);
  }


}
