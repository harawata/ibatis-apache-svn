package org.apache.ibatis.executor.statement;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.result.ResultHandler;
import org.apache.ibatis.mapping.*;

import java.sql.*;
import java.util.List;

public class SimpleStatementHandler extends BaseStatementHandler {

  public SimpleStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameter, int rowOffset, int rowLimit, ResultHandler resultHandler) {
    super(executor, mappedStatement, parameter, rowOffset, rowLimit, resultHandler);
  }

  public int update(Statement statement)
      throws SQLException {
    String sql = boundSql.getSql();
    Object parameterObject = boundSql.getParameterObject();
    if (mappedStatement.isUseGeneratedKeys()) {
      statement.execute(sql, Statement.RETURN_GENERATED_KEYS);
    } else {
      statement.execute(sql);
    }
    int result = statement.getUpdateCount();
    processGeneratedKeys(mappedStatement, statement, parameterObject);
    return result;
  }

  public void batch(Statement statement)
      throws SQLException {
    String sql = boundSql.getSql();
    statement.addBatch(sql);
  }

  public List query(Statement statement, ResultHandler resultHandler)
      throws SQLException {
    String sql = boundSql.getSql();
    statement.execute(sql);
    return resultSetHandler.handleResultSets(statement);
  }

  protected Statement instantiateStatement(Connection connection) throws SQLException {
    if (mappedStatement.getResultSetType() != null) {
      return connection.createStatement(mappedStatement.getResultSetType().getValue(), ResultSet.CONCUR_READ_ONLY);
    } else {
      return connection.createStatement();
    }
  }

  public void parameterize(Statement statement) throws SQLException {
    // N/A
  }

}
