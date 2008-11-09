package org.apache.ibatis.api.defaults;

import org.apache.ibatis.api.SqlSession;
import org.apache.ibatis.api.exceptions.ExceptionFactory;
import org.apache.ibatis.executor.Executor;
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

  public List query(String statement) {
    return query(statement, null);
  }

  public List query(String statement, Object parameter) {
    try {
      MappedStatement ms = configuration.getMappedStatement("com.domain.AuthorMapper.selectAllAuthors");
      return executor.query(ms, parameter , Executor.NO_ROW_OFFSET, Executor.NO_ROW_LIMIT, Executor.NO_RESULT_HANDLER);
    } catch (SQLException e) {
      throw ExceptionFactory.wrapSQLException("Error querying database.  Cause: " + e, e);
    }
  }

}
