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
package com.ibatis.sqlmap.engine.execution;

import com.ibatis.sqlmap.engine.mapping.parameter.BasicParameterMapping;
import com.ibatis.sqlmap.engine.mapping.parameter.ParameterMap;
import com.ibatis.sqlmap.engine.mapping.parameter.ParameterMapping;
import com.ibatis.sqlmap.engine.mapping.result.ResultMap;
import com.ibatis.sqlmap.engine.mapping.statement.RowHandlerCallback;
import com.ibatis.sqlmap.engine.scope.ErrorContext;
import com.ibatis.sqlmap.engine.scope.RequestScope;
import com.ibatis.sqlmap.engine.scope.SessionScope;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqlExecutor {

  //
  // Constants
  //

  public static final int NO_SKIPPED_RESULTS = 0;
  public static final int NO_MAXIMUM_RESULTS = -999999;

  //
  // Public Methods
  //

  /**
   * @param conn
   * @param sql
   * @param parameters
   * @return
   * @throws SQLException
   */
  public int executeUpdate(RequestScope request, Connection conn, String sql, Object[] parameters)
      throws SQLException {
    ErrorContext errorContext = request.getErrorContext();
    errorContext.setActivity("executing update");
    errorContext.setObjectId(sql);

    PreparedStatement ps = null;
    int rows = 0;

    try {
      errorContext.setMoreInfo("Check the SQL Statement (preparation failed).");
      ps = conn.prepareStatement(sql);

      errorContext.setMoreInfo("Check the parameters (set parameters failed).");
      request.getParameterMap().setParameters(request, ps, parameters);

      errorContext.setMoreInfo("Check the statement (update failed).");
      rows = ps.executeUpdate();
    } finally {
      closeStatement(ps);
    }

    return rows;
  }

  /**
   * @param conn
   * @param sql
   * @param parameters
   * @throws SQLException
   */
  public void addBatch(RequestScope request, Connection conn, String sql, Object[] parameters)
      throws SQLException {
    Batch batch = (Batch) request.getSession().getBatch();
    if (batch == null) {
      batch = new Batch();
      request.getSession().setBatch(batch);
    }
    batch.addBatch(request, conn, sql, parameters);
  }

  public int executeBatch(SessionScope session)
      throws SQLException {
    int rows = 0;
    Batch batch = (Batch) session.getBatch();
    if (batch != null) {
      try {
        rows = batch.executeBatch();
      } finally {
        batch.cleanupBatch();
      }
    }
    return rows;
  }

  /**
   * @param conn
   * @param sql
   * @param parameters
   * @param skipResults
   * @param maxResults
   * @throws SQLException
   */
  public void executeQuery(RequestScope request, Connection conn, String sql, Object[] parameters,
                           int skipResults, int maxResults, RowHandlerCallback callback)
      throws SQLException {
    ErrorContext errorContext = request.getErrorContext();
    errorContext.setActivity("executing query");
    errorContext.setObjectId(sql);

    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
      errorContext.setMoreInfo("Check the SQL Statement (preparation failed).");

      Integer rsType = request.getStatement().getResultSetType();
      if (rsType != null) {
        ps = conn.prepareStatement(sql, rsType.intValue(), ResultSet.CONCUR_READ_ONLY);
      } else {
        ps = conn.prepareStatement(sql);
      }

      Integer fetchSize = request.getStatement().getFetchSize();
      if (fetchSize != null) {
        ps.setFetchSize(fetchSize.intValue());
      }

      errorContext.setMoreInfo("Check the parameters (set parameters failed).");
      request.getParameterMap().setParameters(request, ps, parameters);

      errorContext.setMoreInfo("Check the statement (query failed).");
      rs = ps.executeQuery();

      errorContext.setMoreInfo("Check the results (failed to retrieve results).");
      handleResults(request, rs, skipResults, maxResults, callback);

    } finally {
      try {
        closeResultSet(rs);
      } finally {
        closeStatement(ps);
      }
    }

  }

  /**
   * @param conn
   * @param sql
   * @param parameters
   * @return
   * @throws SQLException
   */
  public int executeUpdateProcedure(RequestScope request, Connection conn, String sql, Object[] parameters)
      throws SQLException {
    ErrorContext errorContext = request.getErrorContext();
    errorContext.setActivity("executing update procedure");
    errorContext.setObjectId(sql);

    CallableStatement cs = null;
    int rows = 0;

    try {
      errorContext.setMoreInfo("Check the SQL Statement (preparation failed).");
      cs = conn.prepareCall(sql);

      ParameterMap parameterMap = request.getParameterMap();

      ParameterMapping[] mappings = parameterMap.getParameterMappings();

      errorContext.setMoreInfo("Check the output parameters (register output parameters failed).");
      registerOutputParameters(cs, mappings);

      errorContext.setMoreInfo("Check the parameters (set parameters failed).");
      parameterMap.setParameters(request, cs, parameters);

      errorContext.setMoreInfo("Check the statement (update procedure failed).");
      rows = cs.executeUpdate();

      errorContext.setMoreInfo("Check the output parameters (retrieval of output parameters failed).");
      retrieveOutputParameters(cs, mappings, parameters);
    } finally {
      closeStatement(cs);
    }

    return rows;
  }

  /**
   * @param conn
   * @param sql
   * @param parameters
   * @param skipResults
   * @param maxResults
   * @throws SQLException
   */
  public void executeQueryProcedure(RequestScope request, Connection conn, String sql, Object[] parameters,
                                    int skipResults, int maxResults, RowHandlerCallback callback)
      throws SQLException {
    ErrorContext errorContext = request.getErrorContext();
    errorContext.setActivity("executing query procedure");
    errorContext.setObjectId(sql);

    CallableStatement cs = null;
    ResultSet rs = null;

    try {
      errorContext.setMoreInfo("Check the SQL Statement (preparation failed).");
      cs = conn.prepareCall(sql);

      ParameterMap parameterMap = request.getParameterMap();

      ParameterMapping[] mappings = parameterMap.getParameterMappings();

      errorContext.setMoreInfo("Check the output parameters (register output parameters failed).");
      registerOutputParameters(cs, mappings);

      errorContext.setMoreInfo("Check the parameters (set parameters failed).");
      parameterMap.setParameters(request, cs, parameters);

      errorContext.setMoreInfo("Check the statement (update procedure failed).");
      rs = cs.executeQuery();

      errorContext.setMoreInfo("Check the results (failed to retrieve results).");
      handleResults(request, rs, skipResults, maxResults, callback);

      errorContext.setMoreInfo("Check the output parameters (retrieval of output parameters failed).");
      retrieveOutputParameters(cs, mappings, parameters);

    } finally {
      try {
        closeResultSet(rs);
      } finally {
        closeStatement(cs);
      }
    }

  }

  public void cleanup(SessionScope session) {
    Batch batch = (Batch) session.getBatch();
    if (batch != null) {
      batch.cleanupBatch();
      session.setBatch(null);
    }
  }

  //
  // Private Methods
  //

  private void retrieveOutputParameters(CallableStatement cs, ParameterMapping[] mappings, Object[] parameters) throws SQLException {
    for (int i = 0; i < mappings.length; i++) {
      BasicParameterMapping mapping = ((BasicParameterMapping) mappings[i]);
      if (mapping.isOutputAllowed()) {
        Object o = mapping.getTypeHandler().getResult(cs, i + 1);
        parameters[i] = o;
      }
    }
  }

  private void registerOutputParameters(CallableStatement cs, ParameterMapping[] mappings) throws SQLException {
    for (int i = 0; i < mappings.length; i++) {
      BasicParameterMapping mapping = ((BasicParameterMapping) mappings[i]);
      if (mapping.isOutputAllowed()) {
        cs.registerOutParameter(i + 1, mapping.getJdbcType());
      }
    }
  }

  private void handleResults(RequestScope request, ResultSet rs, int skipResults, int maxResults, RowHandlerCallback callback) throws SQLException {
    ResultMap resultMap = request.getResultMap();
    if (resultMap != null) {
      // Skip Results
      if (rs.getType() != ResultSet.TYPE_FORWARD_ONLY) {
        if (skipResults > 0) {
          rs.absolute(skipResults);
        }
      } else {
        for (int i = 0; i < skipResults; i++) {
          if (!rs.next()) {
            break;
          }
        }
      }

      // Get Results
      int resultsFetched = 0;
      while ((maxResults == SqlExecutor.NO_MAXIMUM_RESULTS || resultsFetched < maxResults) && rs.next()) {
        Object[] columnValues = resultMap.getResults(request, rs);
        callback.handleResultObject(request, columnValues);
        resultsFetched++;
      }
    }
  }

  /**
   * @param ps
   */
  private static void closeStatement(PreparedStatement ps) {
    if (ps != null) {
      try {
        ps.close();
      } catch (SQLException e) {
        // ignore
      }
    }
  }

  /**
   * @param rs
   */
  private static void closeResultSet(ResultSet rs) {
    if (rs != null) {
      try {
        rs.close();
      } catch (SQLException e) {
        // ignore
      }
    }
  }

  //
  // Inner Classes
  //

  private static class Batch {
    private String currentSql;
    private List statementList = new ArrayList();
    private int size;
    private static final int SUCCESS_NO_INFO = -2;
    private static final int EXECUTE_FAILED = -3;

    public Batch() {
      this.size = 0;
    }

    public int getSize() {
      return size;
    }

    public void addBatch(RequestScope request, Connection conn, String sql, Object[] parameters) throws SQLException {
      PreparedStatement ps = null;
      if (currentSql != null
          && sql.hashCode() == currentSql.hashCode()
          && sql.length() == currentSql.length()) {
        int last = statementList.size() - 1;
        ps = (PreparedStatement) statementList.get(last);
      } else {
        ps = conn.prepareStatement(sql);
        currentSql = sql;
        statementList.add(ps);
      }
      request.getParameterMap().setParameters(request, ps, parameters);
      ps.addBatch();
      size++;
    }

    public int executeBatch() throws SQLException {
      int totalRowCount = 0;
      for (int i = 0, n = statementList.size(); i < n; i++) {
        PreparedStatement ps = (PreparedStatement) statementList.get(i);
        int[] rowCounts = ps.executeBatch();
        for (int j = 0; j < rowCounts.length; j++) {
          if (rowCounts[j] == SUCCESS_NO_INFO) {
            // do nothing
          } else if (rowCounts[j] == EXECUTE_FAILED) {
            throw new SQLException("The batched statement at index " + j + " failed to execute.");
          } else {
            totalRowCount += rowCounts[j];
          }
        }
      }
      return totalRowCount;
    }

    public void cleanupBatch() {
      for (int i = 0, n = statementList.size(); i < n; i++) {
        PreparedStatement ps = (PreparedStatement) statementList.get(i);
        closeStatement(ps);
      }
      currentSql = null;
      statementList.clear();
      size = 0;
    }
  }

}
