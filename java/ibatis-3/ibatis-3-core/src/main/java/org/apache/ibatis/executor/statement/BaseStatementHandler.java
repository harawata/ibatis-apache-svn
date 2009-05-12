package org.apache.ibatis.executor.statement;

import org.apache.ibatis.executor.*;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.result.ResultHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.*;
import org.apache.ibatis.type.*;

import java.sql.*;

public abstract class BaseStatementHandler implements StatementHandler {

  protected final ObjectFactory objectFactory;
  protected final TypeHandlerRegistry typeHandlerRegistry;
  protected final ResultSetHandler resultSetHandler;
  protected final ParameterHandler parameterHandler;

  protected final Executor executor;
  protected final MappedStatement mappedStatement;
  protected final int rowOffset;
  protected final int rowLimit;

  protected final BoundSql boundSql;

  protected BaseStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, int rowOffset, int rowLimit, ResultHandler resultHandler) {
    this.executor = executor;
    this.mappedStatement = mappedStatement;
    this.rowOffset = rowOffset;
    this.rowLimit = rowLimit;

    Configuration configuration = mappedStatement.getConfiguration();
    this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
    this.objectFactory = configuration.getObjectFactory();

    this.boundSql = mappedStatement.getBoundSql(parameterObject);

    this.parameterHandler = configuration.newParameterHandler(mappedStatement, parameterObject, boundSql);
    this.resultSetHandler = configuration.newResultSetHandler(executor, mappedStatement, rowOffset, rowLimit, parameterHandler, resultHandler, boundSql);
  }

  public BoundSql getBoundSql() {
    return boundSql;
  }

  public ParameterHandler getParameterHandler() {
    return parameterHandler;
  }

  public Statement prepare(Connection connection)
      throws SQLException {
    ErrorContext.instance().sql(boundSql.getSql());
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


  protected void processGeneratedKeys(MappedStatement ms, Statement stmt, Object parameter) throws SQLException {
    if (parameter != null && ms.isUseGeneratedKeys()) {
      String keyProperty = ms.getKeyProperty();
      final MetaObject metaParam = MetaObject.forObject(parameter);
      if (keyProperty != null && metaParam.hasSetter(keyProperty)) {
        Class keyPropertyType = metaParam.getSetterType(keyProperty);
        TypeHandler th = typeHandlerRegistry.getTypeHandler(keyPropertyType);
        if (th != null) {
          ResultSet rs = stmt.getGeneratedKeys();
          try {
            ResultSetMetaData rsmd = rs.getMetaData();
            int colCount = rsmd.getColumnCount();
            if (colCount > 0) {
              String colName = rsmd.getColumnName(1);
              while (rs.next()) {
                Object value = th.getResult(rs,colName);
                metaParam.setValue(keyProperty,value);
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
      }
    }
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
