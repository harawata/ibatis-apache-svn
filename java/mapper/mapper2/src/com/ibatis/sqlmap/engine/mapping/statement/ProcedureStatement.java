package com.ibatis.sqlmap.engine.mapping.statement;

import com.ibatis.sqlmap.engine.scope.*;

import java.sql.*;

/**
 * User: Clinton Begin
 * Date: Sep 14, 2003
 * Time: 8:19:57 AM
 */
public class ProcedureStatement extends GeneralStatement {

  protected void postProcessParameterObject(RequestScope request, Object parameterObject, Object[] parameters) {
    request.getParameterMap().refreshParameterObjectValues(request, parameterObject, parameters);
  }

  protected int sqlExecuteUpdate(RequestScope request, Connection conn, String sqlString, Object[] parameters) throws SQLException {
    return getSqlExecutor().executeUpdateProcedure(request, conn, sqlString.trim(), parameters);
  }

  protected void sqlExecuteQuery(RequestScope request, Connection conn, String sqlString, Object[] parameters, int skipResults, int maxResults, RowHandlerCallback callback) throws SQLException {
    getSqlExecutor().executeQueryProcedure(request, conn, sqlString.trim(), parameters, skipResults, maxResults, callback);
  }

}
