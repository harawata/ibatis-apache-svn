package com.ibatis.sqlmap.engine.mapping.statement;

import com.ibatis.common.jdbc.exception.NestedSQLException;
import com.ibatis.sqlmap.client.event.RowHandler;
import com.ibatis.sqlmap.engine.execution.SqlExecutor;
import com.ibatis.sqlmap.engine.mapping.parameter.ParameterMap;
import com.ibatis.sqlmap.engine.mapping.result.ResultMap;
import com.ibatis.sqlmap.engine.mapping.sql.Sql;
import com.ibatis.sqlmap.engine.scope.ErrorContext;
import com.ibatis.sqlmap.engine.scope.RequestScope;
import com.ibatis.sqlmap.engine.type.DomTypeMarker;
import com.ibatis.sqlmap.engine.type.XmlTypeMarker;
import org.w3c.dom.Document;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * User: Clinton Begin
 * Date: Sep 6, 2003
 * Time: 9:03:20 AM
 */
public class GeneralStatement extends BaseStatement {

  public int executeUpdate(RequestScope request, Connection conn, Object parameterObject)
      throws SQLException {
    ErrorContext errorContext = request.getErrorContext();
    errorContext.setActivity("preparing the mapped statement for execution");
    errorContext.setObjectId(this.getId());
    errorContext.setResource(this.getResource());

    request.getSession().setCommitRequired(true);

    try {
      validateParameter(parameterObject);

      Sql sql = getSql();

      errorContext.setMoreInfo("Check the parameter map.");
      ParameterMap parameterMap = sql.getParameterMap(request, parameterObject);

      errorContext.setMoreInfo("Check the result map.");
      ResultMap resultMap = sql.getResultMap(request, parameterObject);

      request.setResultMap(resultMap);
      request.setParameterMap(parameterMap);

      int rows = 0;

      errorContext.setMoreInfo("Check the parameter map.");
      Object[] parameters = parameterMap.getParameterObjectValues(request, parameterObject);

      errorContext.setMoreInfo("Check the SQL statement.");
      String sqlString = sql.getSql(request, parameterObject);

      errorContext.setActivity("executing mapped statement");
      errorContext.setMoreInfo("Check the statement or the result map.");
      rows = sqlExecuteUpdate(request, conn, sqlString, parameters);

      errorContext.setMoreInfo("Check the output parameters.");
      if (parameterObject != null) {
        postProcessParameterObject(request, parameterObject, parameters);
      }

      errorContext.reset();
      sql.cleanup(request);
      notifyListeners();
      return rows;
    } catch (SQLException e) {
      errorContext.setCause(e);
      throw new NestedSQLException(errorContext.toString(), e.getSQLState(), e.getErrorCode(), e);
    } catch (Exception e) {
      errorContext.setCause(e);
      throw new NestedSQLException(errorContext.toString(), e);
    }
  }

  public Object executeQueryForObject(RequestScope request, Connection conn, Object parameterObject, Object resultObject)
      throws SQLException {
    Object object = null;

    DefaultRowHandler rowHandler = new DefaultRowHandler();
    executeQueryWithCallback(request, conn, parameterObject, resultObject, rowHandler, SqlExecutor.NO_SKIPPED_RESULTS, SqlExecutor.NO_MAXIMUM_RESULTS);
    List list = rowHandler.getList();

    if (list.size() > 1) {
      throw new SQLException("Error: executeQueryForObject returned too many results.");
    } else if (list.size() > 0) {
      object = list.get(0);
    }

    return object;
  }

  public List executeQueryForList(RequestScope request, Connection conn, Object parameterObject, int skipResults, int maxResults)
      throws SQLException {
    DefaultRowHandler rowHandler = new DefaultRowHandler();
    executeQueryWithCallback(request, conn, parameterObject, null, rowHandler, skipResults, maxResults);
    return rowHandler.getList();
  }

  public void executeQueryWithRowHandler(RequestScope request, Connection conn, Object parameterObject, RowHandler rowHandler)
      throws SQLException {
    executeQueryWithCallback(request, conn, parameterObject, null, rowHandler, SqlExecutor.NO_SKIPPED_RESULTS, SqlExecutor.NO_MAXIMUM_RESULTS);
  }

  //
  //  PROTECTED METHODS
  //

  protected void executeQueryWithCallback(RequestScope request, Connection conn, Object parameterObject, Object resultObject, RowHandler rowHandler, int skipResults, int maxResults)
      throws SQLException {
    ErrorContext errorContext = request.getErrorContext();
    errorContext.setActivity("preparing the mapped statement for execution");
    errorContext.setObjectId(this.getId());
    errorContext.setResource(this.getResource());

    try {
      validateParameter(parameterObject);

      Sql sql = getSql();

      errorContext.setMoreInfo("Check the parameter map.");
      ParameterMap parameterMap = sql.getParameterMap(request, parameterObject);

      errorContext.setMoreInfo("Check the result map.");
      ResultMap resultMap = sql.getResultMap(request, parameterObject);

      request.setResultMap(resultMap);
      request.setParameterMap(parameterMap);

      errorContext.setMoreInfo("Check the parameter map.");
      Object[] parameters = parameterMap.getParameterObjectValues(request, parameterObject);

      errorContext.setMoreInfo("Check the SQL statement.");
      String sqlString = sql.getSql(request, parameterObject);

      errorContext.setActivity("executing mapped statement");
      errorContext.setMoreInfo("Check the SQL statement or the result map.");
      RowHandlerCallback callback = new RowHandlerCallback(resultMap, resultObject, rowHandler);
      sqlExecuteQuery(request, conn, sqlString, parameters, skipResults, maxResults, callback);

      errorContext.setMoreInfo("Check the output parameters.");
      if (parameterObject != null) {
        postProcessParameterObject(request, parameterObject, parameters);
      }

      errorContext.reset();
      sql.cleanup(request);
      notifyListeners();
    } catch (SQLException e) {
      errorContext.setCause(e);
      throw new NestedSQLException(errorContext.toString(), e.getSQLState(), e.getErrorCode(), e);
    } catch (Exception e) {
      errorContext.setCause(e);
      throw new NestedSQLException(errorContext.toString(), e);
    }
  }

  protected void postProcessParameterObject(RequestScope request, Object parameterObject, Object[] parameters) {
  }

  protected int sqlExecuteUpdate(RequestScope request, Connection conn, String sqlString, Object[] parameters) throws SQLException {
    if (request.getSession().isInBatch()) {
      getSqlExecutor().addBatch(request, conn, sqlString, parameters);
      return 0;
    } else {
      return getSqlExecutor().executeUpdate(request, conn, sqlString, parameters);
    }
  }

  protected void sqlExecuteQuery(RequestScope request, Connection conn, String sqlString, Object[] parameters, int skipResults, int maxResults, RowHandlerCallback callback) throws SQLException {
    getSqlExecutor().executeQuery(request, conn, sqlString, parameters, skipResults, maxResults, callback);
  }

  protected void validateParameter(Object param)
      throws SQLException {
    Class parameterClass = getParameterClass();
    if (param != null && parameterClass != null) {
      if (DomTypeMarker.class.isAssignableFrom(parameterClass)) {
        if (!Document.class.isAssignableFrom(param.getClass())) {
          throw new SQLException("Invalid parameter object type.  Expected '" + Document.class.getName() + "' but found '" + param.getClass().getName() + "'.");
        }
      } else if (XmlTypeMarker.class.isAssignableFrom(parameterClass)) {
        if (param.getClass() != String.class) {
          throw new SQLException("Invalid parameter object type.  Expected '" + String.class.getName() + "' but found '" + param.getClass().getName() + "'.");
        }
      } else {
        if (!parameterClass.isAssignableFrom(param.getClass())) {
          throw new SQLException("Invalid parameter object type.  Expected '" + parameterClass.getName() + "' but found '" + param.getClass().getName() + "'.");
        }
      }
    }
  }

}
