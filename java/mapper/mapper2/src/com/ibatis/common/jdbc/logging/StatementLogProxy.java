/**
 * User: Clinton Begin
 * Date: May 22, 2003
 * Time: 8:47:33 PM
 */
package com.ibatis.common.jdbc.logging;

import org.apache.commons.logging.*;

import java.lang.reflect.*;
import java.sql.*;

import com.ibatis.common.beans.ClassInfo;

public class StatementLogProxy extends BaseLogProxy implements InvocationHandler {

  private static final Log log = LogFactory.getLog(Statement.class);

  private Statement statement;

  private StatementLogProxy(Statement stmt) {
    super();
    this.statement = stmt;
  }

  public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
    try {
      if (EXECUTE_METHODS.contains(method.getName())) {
        if (log.isDebugEnabled()) {
          log.debug("{stmt-" + id + "} Statement: " + removeBreakingWhitespace((String) params[0]));
        }
        if ("executeQuery".equals(method.getName())) {
          ResultSet rs = (ResultSet) method.invoke(statement, params);
          return ResultSetLogProxy.newInstance(rs);
        } else {
          return method.invoke(statement, params);
        }
      } else if ("getResultSet".equals(method.getName())) {
        ResultSet rs = (ResultSet) method.invoke(statement, params);
        return ResultSetLogProxy.newInstance(rs);
      } else {
        return method.invoke(statement, params);
      }
    } catch (Throwable t) {
      throw ClassInfo.unwrapThrowable(t);
    }
  }

  public static Statement newInstance(Statement stmt) {
    InvocationHandler handler = new StatementLogProxy(stmt);
    ClassLoader cl = Statement.class.getClassLoader();
    return (Statement) Proxy.newProxyInstance(cl, new Class[]{Statement.class}, handler);
  }

}
