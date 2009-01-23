package org.apache.ibatis.executor.statement;

import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.result.ResultHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.Configuration;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.ObjectFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class BaseStatementHandler implements StatementHandler {

  protected final ObjectFactory objectFactory;
  protected final TypeHandlerRegistry typeHandlerRegistry;
  protected final ResultSetHandler resultSetHandler;
  protected final ParameterHandler parameterHandler;

  protected final Executor executor;
  protected final MappedStatement mappedStatement;
  protected final Object parameterObject;
  protected final int rowOffset;
  protected final int rowLimit;
  protected final String sql;

  protected BaseStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, int rowOffset, int rowLimit, ResultHandler resultHandler) {
    this.executor = executor;
    this.mappedStatement = mappedStatement;
    this.parameterObject = parameterObject;
    this.rowOffset = rowOffset;
    this.rowLimit = rowLimit;

    Configuration configuration = mappedStatement.getConfiguration();
    this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
    this.objectFactory = configuration.getObjectFactory();
    this.parameterHandler = configuration.newParameterHandler(mappedStatement, parameterObject);
    this.resultSetHandler = configuration.newResultSetHandler(executor, mappedStatement, rowOffset, rowLimit, parameterHandler, resultHandler);

    this.sql = mappedStatement.getSql(parameterObject);
  }

  public String getSql() {
    return sql;
  }

  public ParameterHandler getParameterHandler() {
    return parameterHandler;
  }

  public Statement prepare(Connection connection)
      throws SQLException {
    ErrorContext.instance().sql(sql);
    Statement statement = null;
    try {
      statement = instantiateStatement(connection);
      setStatementTimeout(statement);
      setFetchSize(statement);
      return statement;
    } catch (SQLException e) {
      closeStatement(statement);
      throw e;
    } catch (Exception e) {
      closeStatement(statement);
      throw new ExecutorException("Error preparing statement.  Cause: " + e, e);
    }
  }

  protected abstract Statement instantiateStatement(Connection connection)
      throws SQLException;


  protected Integer processGeneratedKeys(MappedStatement ms, Statement stmt, Object parameter) throws SQLException {
    if (ms.getConfiguration().isGeneratedKeysEnabled()) {
      ResultSet rs = stmt.getGeneratedKeys();
      try {
        while (rs.next()) {
          Object object = rs.getObject(1);
          if (object != null) {
            return Integer.parseInt(object.toString());
          }
        }
      } finally {
        try {
          if (rs != null) rs.close();
        } catch (Exception e) {
          //ignore
        }
      }
    }
    return null;
  }

  protected void setStatementTimeout(Statement stmt)
      throws SQLException {
    Integer timeout = mappedStatement.getTimeout();
    Integer defaultTimeout = mappedStatement.getConfiguration().getDefaultStatementTimeout();
    if (timeout != null) {
      stmt.setQueryTimeout(timeout);
    } else if (defaultTimeout != null) {
      stmt.setQueryTimeout(defaultTimeout);
    }
  }

  protected void setFetchSize(Statement stmt)
      throws SQLException {
    Integer fetchSize = mappedStatement.getFetchSize();
    if (fetchSize != null) {
      stmt.setFetchSize(fetchSize);
    }
  }

  protected void closeStatement(Statement statement) {
    try {
      if (statement != null) {
        statement.close();
      }
    } catch (SQLException e) {
      //ignore
    }

  }

}
