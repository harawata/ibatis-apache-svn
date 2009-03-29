package org.apache.ibatis.executor.statement;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.result.ResultHandler;
import org.apache.ibatis.mapping.MappedStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class SimpleStatementHandler extends BaseStatementHandler {

  public SimpleStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameter, int rowOffset, int rowLimit, ResultHandler resultHandler) {
    super(executor, mappedStatement, parameter, rowOffset, rowLimit, resultHandler);
  }

  public int update(Statement statement)
      throws SQLException {
    if (mappedStatement.getConfiguration().isGeneratedKeysEnabled()) {
      statement.execute(sql, Statement.RETURN_GENERATED_KEYS);
    } else {
      statement.execute(sql);
    }
    int result = statement.getUpdateCount();
    if (mappedStatement.getConfiguration().isGeneratedKeysEnabled()) {
      result = processGeneratedKeys(mappedStatement, statement, parameterObject);
    }
    return result;
  }

  public void batch(Statement statement)
      throws SQLException {
    statement.addBatch(sql);
  }

  public List query(Statement statement, ResultHandler resultHandler)
      throws SQLException {
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
