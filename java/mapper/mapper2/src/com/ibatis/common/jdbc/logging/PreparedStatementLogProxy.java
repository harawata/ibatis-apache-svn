/**
 * User: Clinton Begin
 * Date: May 22, 2003
 * Time: 8:44:56 PM
 */
package com.ibatis.common.jdbc.logging;

import java.lang.reflect.*;
import java.sql.*;

import org.apache.commons.logging.*;
import com.ibatis.common.beans.ClassInfo;

public class PreparedStatementLogProxy extends BaseLogProxy implements InvocationHandler {

  private static final Log log = LogFactory.getLog(PreparedStatement.class);

  private PreparedStatement statement;
  private String sql;

  private PreparedStatementLogProxy(PreparedStatement stmt, String sql) {
    this.statement = stmt;
    this.sql = sql;
  }

  public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
    try {
      if (EXECUTE_METHODS.contains(method.getName())) {
        if (log.isDebugEnabled()) {
          log.debug("{pstm-" + id + "} PreparedStatement: " + removeBreakingWhitespace(sql));
          log.debug("{pstm-" + id + "} Parameters: " + getValueString());
          log.debug("{pstm-" + id + "} Types: " + getTypeString());
        }
        clearColumnInfo();
        if ("executeQuery".equals(method.getName())) {
          ResultSet rs = (ResultSet) method.invoke(statement, params);
          return ResultSetLogProxy.newInstance(rs);
        } else {
          return method.invoke(statement, params);
        }
      } else if (SET_METHODS.contains(method.getName())) {
        setColumn(((Integer) params[0]), params[1]);
        return method.invoke(statement, params);
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

  public static PreparedStatement newInstance(PreparedStatement stmt, String sql) {
    InvocationHandler handler = new PreparedStatementLogProxy(stmt, sql);
    ClassLoader cl = PreparedStatement.class.getClassLoader();
    return (PreparedStatement) Proxy.newProxyInstance(cl, new Class[]{PreparedStatement.class, CallableStatement.class}, handler);
  }

}
