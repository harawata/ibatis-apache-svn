package org.apache.ibatis.api.defaults;

import org.apache.ibatis.api.SqlSession;
import org.apache.ibatis.api.ApiException;
import org.apache.ibatis.api.exceptions.ExceptionFactory;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.result.ResultHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.Configuration;

import java.util.List;
import java.sql.SQLException;

public class DefaultSqlSession implements SqlSession {

  private Configuration configuration;
  private Executor executor;

  public DefaultSqlSession(Configuration configuration, Executor executor) {
    this.configuration = configuration;
    this.executor = executor;
  }

  public Object selectOne(String statement) {
    return selectOne(statement,null);
  }

  public Object selectOne(String statement, Object parameter) {
    List list = selectList(statement, parameter);
    if (list.size() != 1) {
      throw new ApiException("Expected one result to be returned by selectOne(), but found: " + list.size());
    }
    return list.get(0);
  }

  public List selectList(String statement) {
    return selectList(statement, null);
  }

  public List selectList(String statement, Object parameter) {
    return selectList(statement, parameter, Executor.NO_ROW_OFFSET, Executor.NO_ROW_LIMIT);
  }

  public List selectList(String statement, Object parameter, int offset, int limit) {
    return selectList(statement, parameter, offset, limit, Executor.NO_RESULT_HANDLER);
  }

  public List selectList(String statement, Object parameter, int offset, int limit, ResultHandler handler) {
    try {
      MappedStatement ms = configuration.getMappedStatement(statement);
      return executor.query(ms, parameter , offset, limit, handler);
    } catch (SQLException e) {
      throw ExceptionFactory.wrapSQLException("Error querying database.  Cause: " + e, e);
    }
  }

  public void close() {
    executor.close();
  }

  public void commit() {
    try {
      executor.commit(false);
    } catch (SQLException e) {
      throw ExceptionFactory.wrapSQLException("Error committing transaction.  Cause: " + e, e);
    }
  }

  public void rollback() {
    try {
      executor.rollback(false);
    } catch (SQLException e) {
      throw ExceptionFactory.wrapSQLException("Error rolling back transaction.  Cause: " + e, e);
    }
  }

}
