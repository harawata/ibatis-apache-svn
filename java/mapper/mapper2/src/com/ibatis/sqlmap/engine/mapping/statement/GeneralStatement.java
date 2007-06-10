/*
 *  Copyright 2004 Clinton Begin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ibatis.sqlmap.engine.mapping.statement;

import com.ibatis.common.jdbc.exception.NestedSQLException;
import com.ibatis.common.io.ReaderInputStream;

import com.ibatis.sqlmap.client.event.RowHandler;
import com.ibatis.sqlmap.engine.execution.SqlExecutor;
import com.ibatis.sqlmap.engine.mapping.parameter.ParameterMap;
import com.ibatis.sqlmap.engine.mapping.result.ResultMap;
import com.ibatis.sqlmap.engine.mapping.sql.Sql;
import com.ibatis.sqlmap.engine.scope.ErrorContext;
import com.ibatis.sqlmap.engine.scope.StatementScope;
import com.ibatis.sqlmap.engine.transaction.Transaction;
import com.ibatis.sqlmap.engine.transaction.TransactionException;
import com.ibatis.sqlmap.engine.type.*;
import org.w3c.dom.Document;

import javax.xml.parsers.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.io.*;

public class GeneralStatement extends BaseStatement {

  public StatementType getStatementType() {
    return StatementType.UNKNOWN;
  }

  public int executeUpdate(StatementScope statementScope, Transaction trans, Object parameterObject)
      throws SQLException {
    ErrorContext errorContext = statementScope.getErrorContext();
    errorContext.setActivity("preparing the mapped statement for execution");
    errorContext.setObjectId(this.getId());
    errorContext.setResource(this.getResource());

    statementScope.getSession().setCommitRequired(true);

    try {
      parameterObject = validateParameter(parameterObject);

      Sql sql = getSql();

      errorContext.setMoreInfo("Check the parameter map.");
      ParameterMap parameterMap = sql.getParameterMap(statementScope, parameterObject);

      errorContext.setMoreInfo("Check the result map.");
      ResultMap resultMap = sql.getResultMap(statementScope, parameterObject);

      statementScope.setResultMap(resultMap);
      statementScope.setParameterMap(parameterMap);

      int rows = 0;

      errorContext.setMoreInfo("Check the parameter map.");
      Object[] parameters = parameterMap.getParameterObjectValues(statementScope, parameterObject);

      errorContext.setMoreInfo("Check the SQL statement.");
      String sqlString = sql.getSql(statementScope, parameterObject);

      errorContext.setActivity("executing mapped statement");
      errorContext.setMoreInfo("Check the statement or the result map.");
      rows = sqlExecuteUpdate(statementScope, trans.getConnection(), sqlString, parameters);

      errorContext.setMoreInfo("Check the output parameters.");
      if (parameterObject != null) {
        postProcessParameterObject(statementScope, parameterObject, parameters);
      }

      errorContext.reset();
      sql.cleanup(statementScope);
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

  public Object executeQueryForObject(StatementScope statementScope, Transaction trans, Object parameterObject, Object resultObject)
      throws SQLException {
    try {
      Object object = null;

      DefaultRowHandler rowHandler = new DefaultRowHandler();
      executeQueryWithCallback(statementScope, trans.getConnection(), parameterObject, resultObject, rowHandler, SqlExecutor.NO_SKIPPED_RESULTS, SqlExecutor.NO_MAXIMUM_RESULTS);
      List list = rowHandler.getList();

      if (list.size() > 1) {
        throw new SQLException("Error: executeQueryForObject returned too many results.");
      } else if (list.size() > 0) {
        object = list.get(0);
      }

      return object;
    } catch (TransactionException e) {
      throw new NestedSQLException("Error getting Connection from Transaction.  Cause: " + e, e);
    }
  }

  public List executeQueryForList(StatementScope statementScope, Transaction trans, Object parameterObject, int skipResults, int maxResults)
      throws SQLException {
    try {
      DefaultRowHandler rowHandler = new DefaultRowHandler();
      executeQueryWithCallback(statementScope, trans.getConnection(), parameterObject, null, rowHandler, skipResults, maxResults);
      return rowHandler.getList();
    } catch (TransactionException e) {
      throw new NestedSQLException("Error getting Connection from Transaction.  Cause: " + e, e);
    }
  }

  public void executeQueryWithRowHandler(StatementScope statementScope, Transaction trans, Object parameterObject, RowHandler rowHandler)
      throws SQLException {
    try {
      executeQueryWithCallback(statementScope, trans.getConnection(), parameterObject, null, rowHandler, SqlExecutor.NO_SKIPPED_RESULTS, SqlExecutor.NO_MAXIMUM_RESULTS);
    } catch (TransactionException e) {
      throw new NestedSQLException("Error getting Connection from Transaction.  Cause: " + e, e);
    }
  }

  //
  //  PROTECTED METHODS
  //

  protected void executeQueryWithCallback(StatementScope statementScope, Connection conn, Object parameterObject, Object resultObject, RowHandler rowHandler, int skipResults, int maxResults)
      throws SQLException {
    ErrorContext errorContext = statementScope.getErrorContext();
    errorContext.setActivity("preparing the mapped statement for execution");
    errorContext.setObjectId(this.getId());
    errorContext.setResource(this.getResource());

    try {
      parameterObject = validateParameter(parameterObject);

      Sql sql = getSql();

      errorContext.setMoreInfo("Check the parameter map.");
      ParameterMap parameterMap = sql.getParameterMap(statementScope, parameterObject);

      errorContext.setMoreInfo("Check the result map.");
      ResultMap resultMap = sql.getResultMap(statementScope, parameterObject);

      statementScope.setResultMap(resultMap);
      statementScope.setParameterMap(parameterMap);

      errorContext.setMoreInfo("Check the parameter map.");
      Object[] parameters = parameterMap.getParameterObjectValues(statementScope, parameterObject);

      errorContext.setMoreInfo("Check the SQL statement.");
      String sqlString = sql.getSql(statementScope, parameterObject);

      errorContext.setActivity("executing mapped statement");
      errorContext.setMoreInfo("Check the SQL statement or the result map.");
      RowHandlerCallback callback = new RowHandlerCallback(resultMap, resultObject, rowHandler);
      sqlExecuteQuery(statementScope, conn, sqlString, parameters, skipResults, maxResults, callback);

      errorContext.setMoreInfo("Check the output parameters.");
      if (parameterObject != null) {
        postProcessParameterObject(statementScope, parameterObject, parameters);
      }

      errorContext.reset();
      sql.cleanup(statementScope);
      notifyListeners();
    } catch (SQLException e) {
      errorContext.setCause(e);
      throw new NestedSQLException(errorContext.toString(), e.getSQLState(), e.getErrorCode(), e);
    } catch (Exception e) {
      errorContext.setCause(e);
      throw new NestedSQLException(errorContext.toString(), e);
    }
  }

  protected void postProcessParameterObject(StatementScope statementScope, Object parameterObject, Object[] parameters) {
  }

  protected int sqlExecuteUpdate(StatementScope statementScope, Connection conn, String sqlString, Object[] parameters) throws SQLException {
    if (statementScope.getSession().isInBatch()) {
      getSqlExecutor().addBatch(statementScope, conn, sqlString, parameters);
      return 0;
    } else {
      return getSqlExecutor().executeUpdate(statementScope, conn, sqlString, parameters);
    }
  }

  protected void sqlExecuteQuery(StatementScope statementScope, Connection conn, String sqlString, Object[] parameters, int skipResults, int maxResults, RowHandlerCallback callback) throws SQLException {
    getSqlExecutor().executeQuery(statementScope, conn, sqlString, parameters, skipResults, maxResults, callback);
  }

  protected Object validateParameter(Object param)
      throws SQLException {
    Object newParam = param;
    Class parameterClass = getParameterClass();
    if (newParam != null && parameterClass != null) {
      if (DomTypeMarker.class.isAssignableFrom(parameterClass)) {
        if (XmlTypeMarker.class.isAssignableFrom(parameterClass)) {
          if (!(newParam instanceof String)
              && !(newParam instanceof Document)) {
            throw new SQLException("Invalid parameter object type.  Expected '" + String.class.getName() + "' or '" + Document.class.getName() + "' but found '" + newParam.getClass().getName() + "'.");
          }
          if (!(newParam instanceof Document)) {
            newParam = stringToDocument ((String)newParam);
          }
        } else {
          if (!Document.class.isAssignableFrom(newParam.getClass())) {
            throw new SQLException("Invalid parameter object type.  Expected '" + Document.class.getName() + "' but found '" + newParam.getClass().getName() + "'.");
          }
        }
      } else {
        if (!parameterClass.isAssignableFrom(newParam.getClass())) {
          throw new SQLException("Invalid parameter object type.  Expected '" + parameterClass.getName() + "' but found '" + newParam.getClass().getName() + "'.");
        }
      }
    }
    return newParam;
  }

  private Document stringToDocument (String s) {
    try {
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      return documentBuilder.parse(new ReaderInputStream(new StringReader(s)));
    } catch (Exception e) {
      throw new RuntimeException("Error occurred.  Cause: " + e, e);
    }
  }
}
