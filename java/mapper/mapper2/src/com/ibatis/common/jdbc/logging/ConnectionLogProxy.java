/**
 * User: Clinton Begin
 * Date: May 22, 2003
 * Time: 8:38:25 PM
 */
package com.ibatis.common.jdbc.logging;

import java.lang.reflect.*;
import java.sql.*;

import org.apache.commons.logging.*;
import com.ibatis.common.beans.ClassInfo;

public class ConnectionLogProxy extends BaseLogProxy implements InvocationHandler {

  private static final Log log = LogFactory.getLog(Connection.class);

  private Connection connection;

  private ConnectionLogProxy(Connection conn) {
    super();
    this.connection = conn;
    if (log.isDebugEnabled()) {
      log.debug("{conn-" + id + "} Connection");
    }
  }

  public Object invoke(Object proxy, Method method, Object[] params)
      throws Throwable {
    try {
      if ("prepareStatement".equals(method.getName())) {
        PreparedStatement stmt = (PreparedStatement) method.invoke(connection, params);
        stmt = PreparedStatementLogProxy.newInstance(stmt, (String) params[0]);
        return stmt;
      } else if ("prepareCall".equals(method.getName())) {
        PreparedStatement stmt = (PreparedStatement) method.invoke(connection, params);
        stmt = PreparedStatementLogProxy.newInstance(stmt, (String) params[0]);
        return stmt;
      } else if ("createStatement".equals(method.getName())) {
        Statement stmt = (Statement) method.invoke(connection, params);
        stmt = StatementLogProxy.newInstance(stmt);
        return stmt;
      } else {
        return method.invoke(connection, params);
      }
    } catch (Throwable t) {
      throw ClassInfo.unwrapThrowable(t);
    }

  }

  public static Connection newInstance(Connection conn) {
    InvocationHandler handler = new ConnectionLogProxy(conn);
    ClassLoader cl = Connection.class.getClassLoader();
    return (Connection) Proxy.newProxyInstance(cl, new Class[]{Connection.class}, handler);
  }

}
