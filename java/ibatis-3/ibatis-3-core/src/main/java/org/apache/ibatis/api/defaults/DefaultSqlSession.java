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

  private boolean autoCommit;
  private boolean dirty;

  public DefaultSqlSession(Configuration configuration, Executor executor, boolean autoCommit) {
    this.configuration = configuration;
    this.executor = executor;
    this.autoCommit = autoCommit;
    this.dirty = false;
  }

  public Object selectOne(String statement) {
    return selectOne(statement, null);
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
      return executor.query(ms, parameter, offset, limit, handler);
    } catch (SQLException e) {
      throw ExceptionFactory.wrapException("Error querying database.  Cause: " + e, e);
    }
  }

  public Object insert(String statement) {
    return insert(statement, null);
  }

  public Object insert(String statement, Object parameter) {
    //TODO: Return selectKey or autogen key.
    return update(statement, parameter);
  }

  public int update(String statement) {
    return update(statement, null);
  }

  public int update(String statement, Object parameter) {
    try {
      //TODO: Need commitRequired option at the statement level
      dirty = true;
      MappedStatement ms = configuration.getMappedStatement(statement);
      return executor.update(ms, parameter);
    } catch (SQLException e) {
      throw ExceptionFactory.wrapException("Error updating database.  Cause: " + e, e);
    }
  }

  public int delete(String statement) {
    return update(statement, null);
  }

  public int delete(String statement, Object parameter) {
    return update(statement, parameter);
  }

  public void commit() {
    try {
      executor.commit(!autoCommit && dirty);
      dirty = false;
    } catch (SQLException e) {
      throw ExceptionFactory.wrapException("Error committing transaction.  Cause: " + e, e);
    }
  }

  public void end() {
    try {
      executor.rollback(!autoCommit && dirty);
      dirty = false;
    } catch (SQLException e) {
      throw ExceptionFactory.wrapException("Error rolling back transaction.  Cause: " + e, e);
    }
  }

  public void close() {
    try {
      try {
        if (dirty) {
          this.end();
        }
      } finally {
        executor.close();
      }
    } catch (Exception e) {
      throw ExceptionFactory.wrapException("Error closing transaction.  Cause: " + e, e);
    }
  }


}
